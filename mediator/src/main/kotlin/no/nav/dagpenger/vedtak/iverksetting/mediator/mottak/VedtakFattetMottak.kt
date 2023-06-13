package no.nav.dagpenger.vedtak.iverksetting.mediator.mottak

import mu.KotlinLogging
import mu.withLoggingContext
import no.nav.dagpenger.vedtak.mediator.IHendelseMediator
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River

internal class VedtakFattetMottak(
    rapidsConnection: RapidsConnection,
    private val hendelseMediator: IHendelseMediator,
) : River.PacketListener {

    private companion object {
        val logger = KotlinLogging.logger { }
    }

    init {
        River(rapidsConnection).apply {
            validate { it.requireValue("@event_name", "vedtak_fattet") }
            validate { it.requireKey("@id", "@opprettet") }
            validate {
                it.requireKey(
                    "ident",
                    "behandlingId",
                    "vedtakId",
                    "vedtaktidspunkt",
                    "virkningsdato",
                    "utbetalingsdager",
                    "utfall",
                )
            }
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        val vedtakId = packet["vedtakId"].asText()
        withLoggingContext("vedtakId" to vedtakId) {
            logger.info { "Fått vedtak_fattet hendelse" }
            val vedtakFattetHendelseMessage = VedtakFattetHendelseMessage(packet)
            vedtakFattetHendelseMessage.behandle(hendelseMediator, context)
        }
    }
}
