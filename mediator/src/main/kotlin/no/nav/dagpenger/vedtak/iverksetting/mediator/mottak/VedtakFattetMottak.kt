package no.nav.dagpenger.vedtak.iverksetting.mediator.mottak

import mu.KotlinLogging
import mu.withLoggingContext
import no.nav.dagpenger.vedtak.iverksetting.mediator.IverksettingMediator
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.MessageProblems
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River

internal class VedtakFattetMottak(
    rapidsConnection: RapidsConnection,
    private val iverksettingMediator: IverksettingMediator,
) : River.PacketListener {

    init {
        River(rapidsConnection).apply {
            validate { it.requireValue("@event_type", "vedtak_fattet") }
            validate { it.requireKey("ident", "behandlingId", "vedtakId", "vedtaktidspunkt", "virkningsdato", "utfall") }
        }.register(this)
    }

    private val logger = KotlinLogging.logger { }
    private val sikkerLogg = KotlinLogging.logger("tjenestekall.VedtakFattetMottak")

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        val vedtakId = packet["vedtakId"].asText()
        withLoggingContext("vedtakId" to vedtakId) {
            val vedtakFattetHendelseMessage = VedtakFattetHendelseMessage(packet)
            iverksettingMediator.håndter(vedtakFattetHendelseMessage.hendelse())
        }
    }

    override fun onError(problems: MessageProblems, context: MessageContext) {
        logger.warn("Kunne ikke lese vedtak_fattet event: $problems (se sikkerlogg for detaljer)")
        sikkerLogg.warn("Kunne ikke lese vedtak_fattet event: ${problems.toExtendedReport()}")
    }
}
