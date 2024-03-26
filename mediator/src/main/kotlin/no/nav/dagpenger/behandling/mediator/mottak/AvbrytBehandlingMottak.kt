package no.nav.dagpenger.behandling.mediator.mottak

import io.opentelemetry.api.trace.Span
import io.opentelemetry.instrumentation.annotations.WithSpan
import mu.KotlinLogging
import mu.withLoggingContext
import no.nav.dagpenger.behandling.mediator.IMessageMediator
import no.nav.dagpenger.behandling.mediator.MessageMediator
import no.nav.dagpenger.behandling.mediator.asUUID
import no.nav.dagpenger.behandling.mediator.melding.HendelseMessage
import no.nav.dagpenger.behandling.modell.hendelser.AvbrytBehandlingHendelse
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.MessageProblems
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River

internal class AvbrytBehandlingMottak(
    rapidsConnection: RapidsConnection,
    private val messageMediator: MessageMediator,
) : River.PacketListener {
    init {
        River(rapidsConnection).apply {
            validate { it.demandValue("@event_name", "avbryt_behandling") }
            validate { it.requireKey("ident") }
            validate { it.requireKey("behandlingId") }
            validate { it.interestedIn("@id", "@opprettet") }
        }.register(this)
    }

    @WithSpan
    override fun onPacket(
        packet: JsonMessage,
        context: MessageContext,
    ) {
        val behandlingId = packet["behandlingId"].asUUID()
        Span.current().apply {
            setAttribute("app.river", name())
            setAttribute("app.behandlingId", behandlingId.toString())
        }
        withLoggingContext(
            "behandlingId" to behandlingId.toString(),
        ) {
            logger.info { "Avbryter behandlingen" }
            val message = AvbrytBehandlingMessage(packet)
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

internal class AvbrytBehandlingMessage(private val packet: JsonMessage) : HendelseMessage(packet) {
    private val hendelse
        get() = AvbrytBehandlingHendelse(id, ident, behandlingId)
    override val ident = packet["ident"].asText()

    private val behandlingId = packet["behandlingId"].asUUID()

    override fun behandle(
        mediator: IMessageMediator,
        context: MessageContext,
    ) {
        mediator.behandle(hendelse, this, context)
    }
}
