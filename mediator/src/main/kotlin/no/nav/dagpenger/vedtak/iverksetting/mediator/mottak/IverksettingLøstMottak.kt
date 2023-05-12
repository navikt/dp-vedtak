package no.nav.dagpenger.vedtak.iverksetting.mediator.mottak

import mu.withLoggingContext
import no.nav.dagpenger.vedtak.mediator.IHendelseMediator
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River

internal class IverksettingLøstMottak(
    rapidsConnection: RapidsConnection,
    private val iverksettingMediator: IHendelseMediator,
) : River.PacketListener {

    init {
        River(rapidsConnection).apply {
            validate { it.demandValue("@event_name", "behov") }
            validate { it.demandAllOrAny("@behov", listOf("Iverksett")) }
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
                    løsning.required("Iverksett")
                }
            }
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        val ident = packet["ident"].asText()
        val vedtakId = packet["vedtakId"].asText()
        val iverksettingId = packet["iverksettingId"].asText()
        withLoggingContext("vedtakId" to vedtakId, "ident" to ident, "iverksettingId" to iverksettingId) {
            val iverksattHendelseMessage = IverksattHendelseMessage(packet)
            iverksattHendelseMessage.behandle(iverksettingMediator, context)
        }
    }
}
