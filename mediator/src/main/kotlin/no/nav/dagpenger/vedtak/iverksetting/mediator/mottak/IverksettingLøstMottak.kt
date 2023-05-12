package no.nav.dagpenger.vedtak.iverksetting.mediator.mottak

import mu.KotlinLogging
import mu.withLoggingContext
import no.nav.dagpenger.vedtak.mediator.IHendelseMediator
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River

internal class IverksettingLøstMottak(
    rapidsConnection: RapidsConnection,
    private val iHendelseMediator: IHendelseMediator,
) : River.PacketListener {

    private companion object {
        val logger = KotlinLogging.logger { }
        val sikkerLogger = KotlinLogging.logger("tjenestekall.IverksettingLøstMottak")
        val behov = "Iverksett"
    }
    init {

        River(rapidsConnection).apply {
            validate { it.demandValue("@event_name", "behov") }
            validate { it.requireKey("@id", "@opprettet") }
            validate { it.demandAllOrAny("@behov", listOf(behov)) }
            validate {
                it.requireKey(
                    "iverksettingId",
                    "ident",
                    "behandlingId",
                    "vedtakId",
                    "@løsning",
                    "@id",
                    "@opprettet",
                )
            }
            validate {
                it.require("@løsning") { løsning ->
                    løsning.required(behov)
                }
            }
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        val ident = packet["ident"].asText()
        val vedtakId = packet["vedtakId"].asText()
        val iverksettingId = packet["iverksettingId"].asText()
        withLoggingContext("vedtakId" to vedtakId, "ident" to ident, "iverksettingId" to iverksettingId) {
            logger.info { "Fått løsning på $behov" }
            sikkerLogger.info { "Fått løsning på $behov med packet\n ${packet.toJson()}" }
            if (vedtakId != "ca852fd7-1364-4f18-824d-33d12e27b9d4") {
                val iverksattHendelseMessage = IverksattHendelseMessage(packet)
                iverksattHendelseMessage.behandle(iHendelseMediator, context)
            }
        }
    }
}
