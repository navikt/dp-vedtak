package no.nav.dagpenger.vedtak.iverksetting.mediator.mottak

import mu.KotlinLogging
import mu.withLoggingContext
import no.nav.dagpenger.vedtak.mediator.IHendelseMediator
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River

internal class DagpengerAvslåttMottak(
    rapidsConnection: RapidsConnection,
    private val hendelseMediator: IHendelseMediator,
) : River.PacketListener {

    private companion object {
        val logger = KotlinLogging.logger { }
        val sikkerlogger = KotlinLogging.logger("tjenestekall.VedtakFattetMottak")
        private val eventNavn = "dagpenger_avslått"
    }

    init {
        River(rapidsConnection).apply {
            validate { it.requireValue("@event_name", eventNavn) }
            validate { it.requireKey("@id", "@opprettet") }
            validate {
                it.requireKey(
                    "ident",
                    "behandlingId",
                    "vedtakId",
                    "vedtaktidspunkt",
                    "virkningsdato",
                )
            }
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        val vedtakId = packet["vedtakId"].asText()
        val behandlingId = packet["behandlingId"].asText()
        withLoggingContext(mapOf("vedtakId" to vedtakId, "behandlingId" to behandlingId)) {
            logger.info { "Fått $eventNavn hendelse" }
            sikkerlogger.info { "Fått $eventNavn hendelse. Hendelse: ${packet.toJson()}" }
            val dagpengerAvslåttMessage = DagpengerAvslåttMessage(packet)
            dagpengerAvslåttMessage.behandle(hendelseMediator, context)
        }
    }
}
