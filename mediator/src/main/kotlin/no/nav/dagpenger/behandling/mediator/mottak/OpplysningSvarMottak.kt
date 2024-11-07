package no.nav.dagpenger.behandling.mediator.mottak

import com.fasterxml.jackson.databind.JsonNode
import com.github.navikt.tbd_libs.rapids_and_rivers.JsonMessage
import com.github.navikt.tbd_libs.rapids_and_rivers.River
import com.github.navikt.tbd_libs.rapids_and_rivers.asLocalDate
import com.github.navikt.tbd_libs.rapids_and_rivers.asLocalDateTime
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageContext
import com.github.navikt.tbd_libs.rapids_and_rivers_api.RapidsConnection
import io.opentelemetry.api.trace.Span
import io.opentelemetry.instrumentation.annotations.WithSpan
import mu.KotlinLogging
import mu.withLoggingContext
import no.nav.dagpenger.behandling.mediator.IMessageMediator
import no.nav.dagpenger.behandling.mediator.MessageMediator
import no.nav.dagpenger.behandling.mediator.OpplysningSvarBygger
import no.nav.dagpenger.behandling.mediator.asUUID
import no.nav.dagpenger.behandling.mediator.melding.HendelseMessage
import no.nav.dagpenger.behandling.mediator.mottak.SvarStrategi.Svar
import no.nav.dagpenger.behandling.modell.hendelser.OpplysningSvar
import no.nav.dagpenger.behandling.modell.hendelser.OpplysningSvar.Tilstand
import no.nav.dagpenger.behandling.modell.hendelser.OpplysningSvarHendelse
import no.nav.dagpenger.behandling.objectMapper
import no.nav.dagpenger.opplysning.BarnDatatype
import no.nav.dagpenger.opplysning.Boolsk
import no.nav.dagpenger.opplysning.Datatype
import no.nav.dagpenger.opplysning.Dato
import no.nav.dagpenger.opplysning.Desimaltall
import no.nav.dagpenger.opplysning.Gyldighetsperiode
import no.nav.dagpenger.opplysning.Heltall
import no.nav.dagpenger.opplysning.InntektDataType
import no.nav.dagpenger.opplysning.Opplysningstype
import no.nav.dagpenger.opplysning.Penger
import no.nav.dagpenger.opplysning.Saksbehandlerkilde
import no.nav.dagpenger.opplysning.Systemkilde
import no.nav.dagpenger.opplysning.Tekst
import no.nav.dagpenger.opplysning.ULID
import no.nav.dagpenger.opplysning.verdier.Barn
import no.nav.dagpenger.opplysning.verdier.BarnListe
import no.nav.dagpenger.opplysning.verdier.Beløp
import no.nav.dagpenger.opplysning.verdier.Inntekt
import no.nav.dagpenger.opplysning.verdier.Ulid
import java.time.LocalDate
import java.util.UUID

internal class OpplysningSvarMottak(
    rapidsConnection: RapidsConnection,
    private val messageMediator: MessageMediator,
    private val opplysningstyper: Set<Opplysningstype<*>>,
) : River.PacketListener {
    init {
        River(rapidsConnection)
            .apply {
                validate { it.demandValue("@event_name", "behov") }
                validate { it.demandValue("@final", true) }
                validate { it.requireValue("@opplysningsbehov", true) }
                validate { it.requireKey("ident") }
                validate { it.requireKey("@løsning") }
                validate { it.requireKey("behandlingId") }
                validate { it.interestedIn("@utledetAv") }
                validate { it.requireValue("@final", true) }
                validate { it.interestedIn("@id", "@opprettet", "@behovId") }
            }.register(this)
    }

    private val skipBehovId = listOf("3f596a29-69a3-4e43-a2ef-629e3ee72921")
    private val skipBehandlingsId = listOf("0191ffac-675a-743c-9042-8e000289a558")

    @WithSpan
    override fun onPacket(
        packet: JsonMessage,
        context: MessageContext,
    ) {
        val behovId = packet["@behovId"].asText()
        val behandlingId = packet["behandlingId"].asUUID()
        addOtelAttributes(behovId, behandlingId)

        withLoggingContext(
            "behovId" to behovId.toString(),
            "behandlingId" to behandlingId.toString(),
        ) {
            if (skipBehovId.contains(behovId)) {
                logger.info { "Mottok svar på en opplysning som skal ignoreres" }
                return
            }
            if (skipBehandlingsId.contains(behandlingId.toString())) {
                logger.info { "Mottok svar på en opplysning som skal ignoreres" }
                return
            }
            logger.info { "Mottok svar på en opplysning" }
            val message = OpplysningSvarMessage(packet, opplysningstyper)
            message.behandle(messageMediator, context)
        }
    }

    private fun addOtelAttributes(
        behovId: String,
        behandlingId: UUID,
    ) {
        Span.current().apply {
            setAttribute("app.river", name())
            setAttribute("app.behovId", behovId)
            setAttribute("app.behandlingId", behandlingId.toString())
        }
    }

    private companion object {
        private val logger = KotlinLogging.logger {}
    }
}

internal class OpplysningSvarMessage(
    private val packet: JsonMessage,
    private val opplysningstyper: Set<Opplysningstype<*>>,
) : HendelseMessage(packet) {
    private val hendelse
        get() =
            OpplysningSvarHendelse(
                id,
                ident,
                behandlingId = packet["behandlingId"].asUUID(),
                opplysninger = opplysning,
                opprettet,
            )
    override val ident get() = packet["ident"].asText()

    private val logger = KotlinLogging.logger {}
    private val sikkerLogger = KotlinLogging.logger("tjenestekall.OpplysningSvarMessage")

    private val opplysning =
        mutableListOf<OpplysningSvar<*>>().apply {
            packet["@løsning"].fields().forEach { (typeNavn, løsning) ->
                logger.info { "Tok i mot opplysning av $typeNavn" }

                val opplysningstype =
                    runCatching { opplysningstyper.single { it.id == typeNavn } }.getOrElse {
                        throw IllegalArgumentException("Ukjent opplysningstype: $typeNavn")
                    }

                val svar = lagSvar(løsning)
                val kilde =
                    when (løsning.has("@kilde")) {
                        true -> {
                            val ident =
                                løsning["@kilde"]["saksbehandler"]?.asText() ?: throw IllegalArgumentException("Mangler saksbehandler")
                            Saksbehandlerkilde(
                                meldingsreferanseId = packet["@id"].asUUID(),
                                opprettet = packet["@opprettet"].asLocalDateTime(),
                                ident = ident,
                            )
                        }

                        false -> {
                            Systemkilde(
                                meldingsreferanseId = packet["@id"].asUUID(),
                                opprettet = packet["@opprettet"].asLocalDateTime(),
                            )
                        }
                    }

                val utledetAv = packet["@utledetAv"][typeNavn]?.map { it.asUUID() } ?: emptyList()

                val opplysningSvarBygger =
                    OpplysningSvarBygger(
                        opplysningstype,
                        JsonMapper(svar.verdi),
                        kilde,
                        svar.tilstand,
                        svar.gyldighetsperiode,
                        utledetAv,
                    )
                val opplysning = opplysningSvarBygger.opplysningSvar()
                add(opplysning)
            }
        }

    override fun behandle(
        mediator: IMessageMediator,
        context: MessageContext,
    ) {
        withLoggingContext(hendelse.kontekstMap()) {
            logger.info { "Behandler svar på opplysninger: ${hendelse.opplysninger.map { it.opplysningstype.id }}" }
            mediator.behandle(hendelse, this, context)
        }
    }

    private companion object {
        private val svarStrategier = listOf(KomplekstSvar, EnkeltSvar)

        fun lagSvar(jsonNode: JsonNode): Svar = svarStrategier.firstNotNullOf { it.svar(jsonNode) }
    }

    private fun JsonNode.somListe(): List<JsonNode> =
        when (isArray) {
            true -> toList()
            false -> listOf(this)
        }
}

private fun interface SvarStrategi {
    fun svar(svar: JsonNode): Svar?

    data class Svar(
        val verdi: JsonNode,
        val tilstand: Tilstand,
        val gyldigFraOgMed: LocalDate? = null,
        val gyldigTilOgMed: LocalDate? = null,
    ) {
        val gyldighetsperiode
            get() =
                if (gyldigFraOgMed != null && gyldigTilOgMed != null) {
                    Gyldighetsperiode(gyldigFraOgMed, gyldigTilOgMed)
                } else if (gyldigFraOgMed != null && gyldigTilOgMed == null) {
                    Gyldighetsperiode(gyldigFraOgMed)
                } else if (gyldigTilOgMed != null) {
                    Gyldighetsperiode(tom = gyldigTilOgMed)
                } else {
                    Gyldighetsperiode()
                }
    }
}

private object KomplekstSvar : SvarStrategi {
    override fun svar(svar: JsonNode): Svar? {
        if (!svar.isObject) return null
        return Svar(
            svar["verdi"],
            svar["status"]?.asText()?.let { Tilstand.valueOf(it) } ?: Tilstand.Faktum,
            svar["gyldigFraOgMed"]?.asLocalDate(),
            svar["gyldigTilOgMed"]?.asLocalDate(),
        )
    }
}

private object EnkeltSvar : SvarStrategi {
    override fun svar(svar: JsonNode): Svar? {
        if (svar.isObject) return null
        return Svar(svar, Tilstand.Faktum)
    }
}

@Suppress("UNCHECKED_CAST")
private class JsonMapper(
    private val verdi: JsonNode,
) : OpplysningSvarBygger.VerdiMapper {
    override fun <T : Comparable<T>> map(datatype: Datatype<T>): T =
        when (datatype) {
            Dato -> verdi.asLocalDate() as T
            Heltall -> verdi.asInt() as T
            Desimaltall -> verdi.asDouble() as T
            Boolsk -> verdi.asBoolean() as T
            ULID -> Ulid(verdi.asText()) as T
            Penger -> Beløp(verdi.asText().toBigDecimal()) as T
            BarnDatatype -> barnMapper(verdi) as T
            InntektDataType ->
                Inntekt(
                    objectMapper.convertValue(verdi, no.nav.dagpenger.inntekt.v1.Inntekt::class.java),
                ) as T

            Tekst -> verdi.asText() as T
        }
}

private fun barnMapper(verdi: JsonNode): BarnListe {
    return BarnListe(
        barn =
            verdi.map {
                Barn(
                    fødselsdato = it["fødselsdato"].asLocalDate(),
                    fornavnOgMellomnavn = it["fornavnOgMellomnavn"]?.asText(),
                    etternavn = it["etternavn"]?.asText(),
                    statsborgerskap = it["statsborgerskap"]?.asText(),
                    kvalifiserer = it["kvalifiserer"].asBoolean(),
                )
            },
    )
}
