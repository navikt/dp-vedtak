package no.nav.dagpenger.behandling.mediator.mottak

import com.github.navikt.tbd_libs.rapids_and_rivers.JsonMessage
import com.github.navikt.tbd_libs.rapids_and_rivers.River
import com.github.navikt.tbd_libs.rapids_and_rivers.asLocalDate
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageContext
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageProblems
import com.github.navikt.tbd_libs.rapids_and_rivers_api.RapidsConnection
import io.opentelemetry.api.trace.Span
import io.opentelemetry.instrumentation.annotations.WithSpan
import mu.KotlinLogging
import mu.withLoggingContext
import no.nav.dagpenger.behandling.mediator.IMessageMediator
import no.nav.dagpenger.behandling.mediator.MessageMediator
import no.nav.dagpenger.behandling.mediator.asUUID
import no.nav.dagpenger.behandling.mediator.melding.HendelseMessage
import no.nav.dagpenger.behandling.modell.hendelser.NyPrøvingsdatoHendelse

internal class NyPrøvingsdatoMottak(
    rapidsConnection: RapidsConnection,
    private val messageMediator: MessageMediator,
) : River.PacketListener {
    init {
        River(rapidsConnection)
            .apply {
                validate { it.demandValue("@event_name", "ny_prøvingsdato") }
                validate { it.requireKey("ident") }
                validate { it.requireKey("behandlingId") }
                validate { it.requireKey("prøvingsdato") }
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
            logger.info { "Setter prøvingsdato på behandlingen" }
            val message = NyPrøvingsdatoMessage(packet)
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

internal class NyPrøvingsdatoMessage(
    packet: JsonMessage,
) : HendelseMessage(packet) {
    private val hendelse
        get() = NyPrøvingsdatoHendelse(id, ident, behandlingId, prøvingsdato, opprettet)
    override val ident = packet["ident"].asText()
    private val prøvingsdato = packet["prøvingsdato"].asLocalDate()

    private val behandlingId = packet["behandlingId"].asUUID()

    override fun behandle(
        mediator: IMessageMediator,
        context: MessageContext,
    ) {
        mediator.behandle(hendelse, this, context)
    }
}
