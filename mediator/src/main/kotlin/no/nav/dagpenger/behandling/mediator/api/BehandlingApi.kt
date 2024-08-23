package no.nav.dagpenger.behandling.mediator.api

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.application.createApplicationPlugin
import io.ktor.server.application.install
import io.ktor.server.auth.authenticate
import io.ktor.server.plugins.swagger.swaggerUI
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.put
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import io.opentelemetry.api.trace.Span
import no.nav.dagpenger.aktivitetslogg.AuditOperasjon
import no.nav.dagpenger.behandling.api.models.BehandlingDTO
import no.nav.dagpenger.behandling.api.models.DataTypeDTO
import no.nav.dagpenger.behandling.api.models.IdentForesporselDTO
import no.nav.dagpenger.behandling.api.models.OppdaterOpplysningRequestDTO
import no.nav.dagpenger.behandling.api.models.OpplysningDTO
import no.nav.dagpenger.behandling.api.models.OpplysningskildeDTO
import no.nav.dagpenger.behandling.api.models.OpplysningstypeDTO
import no.nav.dagpenger.behandling.api.models.RegelDTO
import no.nav.dagpenger.behandling.api.models.UtledningDTO
import no.nav.dagpenger.behandling.mediator.OpplysningSvarBygger
import no.nav.dagpenger.behandling.mediator.OpplysningSvarBygger.VerdiMapper
import no.nav.dagpenger.behandling.mediator.PersonMediator
import no.nav.dagpenger.behandling.mediator.api.auth.saksbehandlerId
import no.nav.dagpenger.behandling.mediator.audit.Auditlogg
import no.nav.dagpenger.behandling.mediator.repository.PersonRepository
import no.nav.dagpenger.behandling.modell.Behandling
import no.nav.dagpenger.behandling.modell.Ident.Companion.tilPersonIdentfikator
import no.nav.dagpenger.behandling.modell.UUIDv7
import no.nav.dagpenger.behandling.modell.hendelser.AvbrytBehandlingHendelse
import no.nav.dagpenger.behandling.modell.hendelser.ForslagGodkjentHendelse
import no.nav.dagpenger.behandling.modell.hendelser.OpplysningSvar
import no.nav.dagpenger.behandling.modell.hendelser.OpplysningSvarHendelse
import no.nav.dagpenger.opplysning.Boolsk
import no.nav.dagpenger.opplysning.Datatype
import no.nav.dagpenger.opplysning.Dato
import no.nav.dagpenger.opplysning.Desimaltall
import no.nav.dagpenger.opplysning.Faktum
import no.nav.dagpenger.opplysning.Heltall
import no.nav.dagpenger.opplysning.Hypotese
import no.nav.dagpenger.opplysning.InntektDataType
import no.nav.dagpenger.opplysning.Opplysning
import no.nav.dagpenger.opplysning.Opplysningstype
import no.nav.dagpenger.opplysning.Penger
import no.nav.dagpenger.opplysning.Redigerbar
import no.nav.dagpenger.opplysning.Saksbehandlerkilde
import no.nav.dagpenger.opplysning.Systemkilde
import no.nav.dagpenger.opplysning.Tekst
import no.nav.dagpenger.opplysning.ULID
import no.nav.dagpenger.opplysning.verdier.Beløp
import no.nav.dagpenger.regel.TapAvArbeidsinntektOgArbeidstid
import no.nav.dagpenger.regel.Verneplikt
import org.apache.kafka.common.errors.ResourceNotFoundException
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.util.UUID

internal fun Application.behandlingApi(
    personRepository: PersonRepository,
    personMediator: PersonMediator,
    auditlogg: Auditlogg,
    opplysningstyper: Set<Opplysningstype<*>>,
) {
    konfigurerApi()
    install(OtelTraceIdPlugin)

    routing {
        swaggerUI(path = "openapi", swaggerFile = "behandling-api.yaml")

        get("/") { call.respond(HttpStatusCode.OK) }
        get("/opplysningstyper") {
            val typer =
                opplysningstyper.map {
                    OpplysningstypeDTO(
                        it.id,
                        it.navn,
                        when (it.datatype) {
                            Boolsk -> DataTypeDTO.boolsk
                            Dato -> DataTypeDTO.dato
                            Desimaltall -> DataTypeDTO.desimaltall
                            Heltall -> DataTypeDTO.heltall
                            ULID -> DataTypeDTO.ulid
                            Penger -> DataTypeDTO.penger
                            InntektDataType -> DataTypeDTO.inntekt
                            Tekst -> DataTypeDTO.tekst
                        },
                    )
                }
            call.respond(HttpStatusCode.OK, typer)
        }

        authenticate("azureAd") {
            route("behandling") {
                post {
                    val identForespørsel = call.receive<IdentForesporselDTO>()
                    val ident = identForespørsel.ident
                    val person =
                        personRepository.hent(
                            ident.tilPersonIdentfikator(),
                        ) ?: throw ResourceNotFoundException("Person ikke funnet")

                    auditlogg.les("Listet ut behandlinger", ident, call.saksbehandlerId())

                    call.respond(HttpStatusCode.OK, person.behandlinger().map { it.tilBehandlingDTO() })
                }

                route("{behandlingId}") {
                    get {
                        val behandling =
                            personRepository.hentBehandling(
                                call.behandlingId,
                            ) ?: throw ResourceNotFoundException("Behandling ikke funnet")

                        auditlogg.les("Så en behandling", behandling.behandler.ident, call.saksbehandlerId())

                        call.respond(HttpStatusCode.OK, behandling.tilBehandlingDTO())
                    }

                    post("godkjenn") {
                        val identForespørsel = call.receive<IdentForesporselDTO>()
                        // TODO: Her må vi virkelig finne ut hva vi skal gjøre. Dette er bare en placeholder
                        val hendelse = ForslagGodkjentHendelse(UUIDv7.ny(), identForespørsel.ident, call.behandlingId, LocalDateTime.now())
                        hendelse.info("Godkjente behandling", identForespørsel.ident, call.saksbehandlerId(), AuditOperasjon.UPDATE)

                        personMediator.håndter(hendelse)

                        call.respond(HttpStatusCode.Created)
                    }

                    post("avbryt") {
                        val identForespørsel = call.receive<IdentForesporselDTO>()
                        // TODO: Her må vi virkelig finne ut hva vi skal gjøre. Dette er bare en placeholder
                        val hendelse =
                            AvbrytBehandlingHendelse(
                                UUIDv7.ny(),
                                identForespørsel.ident,
                                call.behandlingId,
                                "Avbrutt av saksbehandler",
                                LocalDateTime.now(),
                            )
                        hendelse.info("Avbrøt behandling", identForespørsel.ident, call.saksbehandlerId(), AuditOperasjon.UPDATE)

                        personMediator.håndter(hendelse)

                        call.respond(HttpStatusCode.Created)
                    }

                    put("opplysning/{opplysningId}") {
                        val behandlingId = call.behandlingId
                        val opplysningId = call.opplysningId
                        val oppdaterOpplysningRequestDTO = call.receive<OppdaterOpplysningRequestDTO>()
                        val behandling =
                            personRepository.hentBehandling(behandlingId) ?: throw ResourceNotFoundException("Behandling ikke funnet")
                        val opplysning = behandling.opplysninger().finnOpplysning(opplysningId)
                        val opplysningSvarBygger =
                            OpplysningSvarBygger(
                                opplysning.opplysningstype,
                                HttpVerdiMapper(oppdaterOpplysningRequestDTO),
                                Saksbehandlerkilde(call.saksbehandlerId()),
                                OpplysningSvar.Tilstand.Faktum,
                                opplysning.gyldighetsperiode,
                            )
                        val svar =
                            OpplysningSvarHendelse(
                                UUIDv7.ny(),
                                behandling.behandler.ident,
                                behandling.behandlingId,
                                listOf(opplysningSvarBygger.opplysningSvar()),
                                LocalDateTime.now(),
                            )

                        auditlogg.oppdater("Oppdaterte opplysning", behandling.behandler.ident, call.saksbehandlerId())
                        svar.info(
                            "Oppdaterte ${opplysning.opplysningstype.navn}, begrunnelse ${oppdaterOpplysningRequestDTO.begrunnelse}",
                        )
                        behandling.håndter(svar)
                        personRepository.lagre(behandling)
                        call.respond(HttpStatusCode.OK)
                    }
                }
            }
        }
    }
}

private val ApplicationCall.opplysningId: UUID
    get() {
        val opplysningId = parameters["opplysningId"] ?: throw IllegalArgumentException("OpplysningId må være satt")
        return UUID.fromString(opplysningId)
    }
private val ApplicationCall.behandlingId: UUID
    get() {
        val behandlingId = parameters["behandlingId"] ?: throw IllegalArgumentException("BehandlingId må være satt")
        return UUID.fromString(behandlingId)
    }

private val OtelTraceIdPlugin =
    createApplicationPlugin("OtelTraceIdPlugin") {
        onCallRespond { call, _ ->
            val traceId = runCatching { Span.current().spanContext.traceId }.getOrNull()
            traceId?.let { call.response.headers.append("X-Trace-Id", it) }
        }
    }

private fun Behandling.tilBehandlingDTO(): BehandlingDTO =
    BehandlingDTO(
        behandlingId = this.behandlingId,
        tilstand = BehandlingDTO.Tilstand.valueOf(tilstand().first.name),
        opplysning =
            this.opplysninger().finnAlle().map { opplysning ->
                opplysning.tilOpplysningDTO()
            },
    )

private fun Opplysning<*>.tilOpplysningDTO(): OpplysningDTO =
    OpplysningDTO(
        id = this.id,
        navn = this.opplysningstype.navn,
        verdi =
            when (this.opplysningstype.datatype) {
                // todo: Frontenden burde vite om det er penger og håndtere det med valuta
                Penger -> (this.verdi as Beløp).uavrundet.toString()
                else -> this.verdi.toString()
            },
        status =
            when (this) {
                is Faktum -> OpplysningDTO.Status.Faktum
                is Hypotese -> OpplysningDTO.Status.Hypotese
            },
        gyldigFraOgMed = this.gyldighetsperiode.fom.tilApiDato(),
        gyldigTilOgMed = this.gyldighetsperiode.tom.tilApiDato(),
        datatype =
            when (this.opplysningstype.datatype) {
                Boolsk -> DataTypeDTO.boolsk
                Dato -> DataTypeDTO.dato
                Desimaltall -> DataTypeDTO.desimaltall
                Heltall -> DataTypeDTO.heltall
                ULID -> DataTypeDTO.ulid
                Penger -> DataTypeDTO.penger
                InntektDataType -> DataTypeDTO.inntekt
                Tekst -> DataTypeDTO.tekst
            },
        kilde =
            this.kilde?.let {
                val registrert = it.registrert.tilOffsetTime()
                when (it) {
                    is Saksbehandlerkilde -> OpplysningskildeDTO("Saksbehandler", ident = it.ident, registrert = registrert)
                    is Systemkilde -> OpplysningskildeDTO("System", meldingId = it.meldingsreferanseId, registrert = registrert)
                }
            },
        utledetAv =
            this.utledetAv?.let { utledning ->
                UtledningDTO(
                    regel = RegelDTO(navn = utledning.regel),
                    opplysninger = utledning.opplysninger.map { it.tilOpplysningDTO() },
                )
            },
        redigerbar =
            this.kanRedigere(redigerbarPerOpplysningstype),
    )

private fun LocalDate.tilApiDato(): LocalDate? =
    when (this) {
        LocalDate.MIN -> null
        LocalDate.MAX -> null
        else -> this
    }

// TODO: Denne bor nok et annet sted - men bare for å vise at det er mulig å ha en slik funksjon
val redigerbarPerOpplysningstype =
    object : Redigerbar {
        private val redigerbare =
            setOf(
                TapAvArbeidsinntektOgArbeidstid.beregnetArbeidstid,
                TapAvArbeidsinntektOgArbeidstid.nyArbeidstid,
                Verneplikt.avtjentVerneplikt,
            )

        override fun kanRedigere(opplysning: Opplysning<*>): Boolean = redigerbare.contains(opplysning.opplysningstype)
    }

private fun LocalDateTime.tilOffsetTime(): OffsetDateTime = this.atZone(ZoneId.systemDefault()).toOffsetDateTime()

@Suppress("UNCHECKED_CAST")
class HttpVerdiMapper(
    private val oppdaterOpplysningRequestDTO: OppdaterOpplysningRequestDTO,
) : VerdiMapper {
    override fun <T : Comparable<T>> map(datatype: Datatype<T>): T =
        when (datatype) {
            Heltall -> oppdaterOpplysningRequestDTO.verdi.toInt() as T
            Boolsk -> oppdaterOpplysningRequestDTO.verdi.toBoolean() as T
            else -> throw IllegalArgumentException("Datatype $datatype støttes ikke i APIet enda")
        }
}
