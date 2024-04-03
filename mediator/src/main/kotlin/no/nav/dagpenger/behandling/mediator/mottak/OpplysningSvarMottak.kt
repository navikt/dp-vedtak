package no.nav.dagpenger.behandling.mediator.mottak

import com.fasterxml.jackson.databind.JsonNode
import io.opentelemetry.api.trace.Span
import io.opentelemetry.instrumentation.annotations.WithSpan
import mu.KotlinLogging
import mu.withLoggingContext
import no.nav.dagpenger.behandling.mediator.IMessageMediator
import no.nav.dagpenger.behandling.mediator.MessageMediator
import no.nav.dagpenger.behandling.mediator.asUUID
import no.nav.dagpenger.behandling.mediator.melding.HendelseMessage
import no.nav.dagpenger.behandling.modell.hendelser.OpplysningSvar
import no.nav.dagpenger.behandling.modell.hendelser.OpplysningSvar.Tilstand
import no.nav.dagpenger.behandling.modell.hendelser.OpplysningSvarHendelse
import no.nav.dagpenger.opplysning.Boolsk
import no.nav.dagpenger.opplysning.Dato
import no.nav.dagpenger.opplysning.Desimaltall
import no.nav.dagpenger.opplysning.Gyldighetsperiode
import no.nav.dagpenger.opplysning.Heltall
import no.nav.dagpenger.opplysning.Kilde
import no.nav.dagpenger.opplysning.Opplysningstype
import no.nav.dagpenger.opplysning.Systemkilde
import no.nav.dagpenger.opplysning.ULID
import no.nav.dagpenger.opplysning.verdier.Ulid
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.MessageProblems
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River
import no.nav.helse.rapids_rivers.asLocalDate
import no.nav.helse.rapids_rivers.asLocalDateTime
import java.time.LocalDate

internal class OpplysningSvarMottak(
    rapidsConnection: RapidsConnection,
    private val messageMediator: MessageMediator,
) : River.PacketListener {
    init {
        River(rapidsConnection).apply {
            validate { it.demandValue("@event_name", "behov") }
            validate { it.demandValue("@final", true) }
            validate { it.demandValue("@opplysningsbehov", true) }
            validate { it.requireKey("ident") }
            validate { it.requireKey("@løsning") }
            validate { it.requireKey("behandlingId") }
            validate { it.interestedIn("@id", "@opprettet", "@behovId") }
        }.register(this)
    }

    @WithSpan
    override fun onPacket(
        packet: JsonMessage,
        context: MessageContext,
    ) {
        val behovId = packet["@behovId"].asText()
        val behandlingId = packet["behandlingId"].asUUID()
        Span.current().apply {
            setAttribute("app.river", name())
            setAttribute("app.behovId", behovId.toString())
            setAttribute("app.behandlingId", behandlingId.toString())
        }
        withLoggingContext(
            "behovId" to behovId.toString(),
            "behandlingId" to behandlingId.toString(),
        ) {
            logger.info { "Mottok svar på en opplysning" }
            val message = OpplysningSvarMessage(packet)
            message.behandle(messageMediator, context)
        }
    }

    override fun onError(
        problems: MessageProblems,
        context: MessageContext,
    ) {
        logger.error { problems }
    }

    private companion object {
        private val logger = KotlinLogging.logger {}
    }
}

internal class OpplysningSvarMessage(private val packet: JsonMessage) : HendelseMessage(packet) {
    private val hendelse
        get() =
            OpplysningSvarHendelse(
                id,
                ident,
                behandlingId = packet["behandlingId"].asUUID(),
                opplysninger = opplysning,
            )
    override val ident get() = packet["ident"].asText()

    private val logger = KotlinLogging.logger {}

    private val opplysning =
        mutableListOf<OpplysningSvar<*>>().apply {
            packet["@løsning"].fields().forEach { (typeNavn, jsonVerdi) ->
                logger.info { "Tok i mot opplysning av $typeNavn" }

                if (Opplysningstype.typer.find { it.id == typeNavn } == null) {
                    logger.error { "Ukjent opplysningstype: $typeNavn" }
                    return@forEach
                }
                // @todo: Forventer at verdi er en nøkkel på alle løsninger men vi må skrive om behovløserne for å få dette til å stemme
                val svar =
                    when (jsonVerdi.isObject) {
                        true ->
                            Svar(
                                jsonVerdi["verdi"],
                                jsonVerdi["gyldigFraOgMed"]?.asLocalDate(),
                                jsonVerdi["gyldigTilOgMed"]?.asLocalDate(),
                            )

                        false -> Svar(jsonVerdi)
                    }
                val type = Opplysningstype.typer.single { opplysningstype -> opplysningstype.id == typeNavn }
                val kilde = Systemkilde(meldingsreferanseId = packet["@id"].asUUID(), opprettet = packet["@opprettet"].asLocalDateTime())

                val opplysning =
                    @Suppress("UNCHECKED_CAST")
                    when (type.datatype) {
                        Dato -> opplysningSvar(type as Opplysningstype<LocalDate>, svar.verdi.asLocalDate(), kilde, svar.gyldighetsperiode)
                        Heltall -> opplysningSvar(type as Opplysningstype<Int>, svar.verdi.asInt(), kilde, svar.gyldighetsperiode)
                        Desimaltall -> opplysningSvar(type as Opplysningstype<Double>, svar.verdi.asDouble(), kilde, svar.gyldighetsperiode)
                        Boolsk -> opplysningSvar(type as Opplysningstype<Boolean>, svar.verdi.asBoolean(), kilde, svar.gyldighetsperiode)
                        ULID -> opplysningSvar(type as Opplysningstype<Ulid>, Ulid(svar.verdi.asText()), kilde, svar.gyldighetsperiode)
                    }
                add(opplysning)
            }
        }

    private data class Svar(val verdi: JsonNode, val gyldigFraOgMed: LocalDate? = null, val gyldigTilOgMed: LocalDate? = null) {
        val gyldighetsperiode
            get() =
                if (gyldigFraOgMed != null && gyldigTilOgMed != null) {
                    Gyldighetsperiode(gyldigFraOgMed, gyldigTilOgMed)
                } else if (gyldigFraOgMed != null && gyldigTilOgMed == null) {
                    Gyldighetsperiode(gyldigFraOgMed)
                } else if (gyldigTilOgMed != null) {
                    Gyldighetsperiode(tom = gyldigTilOgMed.atStartOfDay())
                } else {
                    null
                }
    }

    private fun <T : Comparable<T>> opplysningSvar(
        type: Opplysningstype<T>,
        verdi: T,
        kilde: Kilde,
        gyldighetsperiode: Gyldighetsperiode? = null,
    ) = OpplysningSvar(
        opplysningstype = type,
        verdi = verdi,
        tilstand = Tilstand.Hypotese,
        kilde = kilde,
        gyldighetsperiode = gyldighetsperiode,
    )

    override fun behandle(
        mediator: IMessageMediator,
        context: MessageContext,
    ) {
        withLoggingContext(hendelse.kontekstMap()) {
            logger.info { "Behandler svar på opplysning" }
            mediator.behandle(hendelse, this, context)
        }
    }
}
