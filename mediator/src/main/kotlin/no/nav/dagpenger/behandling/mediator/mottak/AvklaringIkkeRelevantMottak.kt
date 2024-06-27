package no.nav.dagpenger.behandling.mediator.mottak

import mu.KotlinLogging
import mu.withLoggingContext
import no.nav.dagpenger.behandling.mediator.IMessageMediator
import no.nav.dagpenger.behandling.mediator.MessageMediator
import no.nav.dagpenger.behandling.mediator.melding.HendelseMessage
import no.nav.dagpenger.behandling.modell.hendelser.AvklaringIkkeRelevantHendelse
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River

internal class AvklaringIkkeRelevantMottak(
    rapidsConnection: RapidsConnection,
    private val messageMediator: MessageMediator,
) : River.PacketListener {
    private companion object {
        private val logger = KotlinLogging.logger {}
    }

    init {
        River(rapidsConnection)
            .apply {
                validate { it.demandValue("@event_name", "AvklaringIkkeRelevant") }
                validate { it.requireKey("ident") }
                validate { it.requireKey("avklaringId", "kode") }
                validate { it.requireKey("behandlingId") }
                validate { it.interestedIn("@id", "@opprettet", "@behovId") }
            }.register(this)
    }

    override fun onPacket(
        packet: JsonMessage,
        context: MessageContext,
    ) {
        withLoggingContext(
            "behovId" to packet["@behovId"].asText(),
            "behandlingId" to packet["behandlingId"].asText(),
            "avklaringId" to packet["avklaringId"].asText(),
        ) {
            logger.info { "Mottok avklaring for ${packet["kode"].asText()}" }
            val message = AvklaringIkkeRelevantMessage(packet)
            message.behandle(messageMediator, context)
        }
    }
}

internal class AvklaringIkkeRelevantMessage(
    packet: JsonMessage,
) : HendelseMessage(packet) {
    override val ident = packet["ident"].asText()

    private val hendelse
        get() = AvklaringIkkeRelevantHendelse(id, ident, avklaringId, kode, behandlingId)

    private val avklaringId = packet["avklaringId"].asUUID()
    private val behandlingId = packet["behandlingId"].asUUID()
    private val kode = packet["kode"].asText()

    override fun behandle(
        mediator: IMessageMediator,
        context: MessageContext,
    ) {
        mediator.behandle(hendelse, this, context)
    }
}
