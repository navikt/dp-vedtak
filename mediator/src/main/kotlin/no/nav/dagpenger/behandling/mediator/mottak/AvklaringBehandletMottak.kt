package no.nav.dagpenger.behandling.mediator.mottak

import io.opentelemetry.api.trace.Span
import io.opentelemetry.instrumentation.annotations.WithSpan
import mu.KotlinLogging
import mu.withLoggingContext
import no.nav.dagpenger.behandling.mediator.IMessageMediator
import no.nav.dagpenger.behandling.mediator.MessageMediator
import no.nav.dagpenger.behandling.mediator.asUUID
import no.nav.dagpenger.behandling.mediator.melding.HendelseMessage
import no.nav.dagpenger.behandling.modell.hendelser.ManuellBehandlingAvklartHendelse
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River
import java.util.UUID

internal class AvklaringBehandletMottak(
    rapidsConnection: RapidsConnection,
    private val messageMediator: MessageMediator,
) : River.PacketListener {
    init {
        River(rapidsConnection)
            .apply {
                validate { it.demandValue("@event_name", "AvklaringBehandlet") }
                validate { it.requireKey("ident") }
                validate { it.requireKey("utfall") }
                validate { it.requireKey("behandlingId") }
                validate { it.requireValue("@final", true) }
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
        addOtelAttributes(behovId, behandlingId)
        withLoggingContext(
            "behovId" to behovId.toString(),
            "behandlingId" to behandlingId.toString(),
        ) {
            logger.info { "Mottok avklaring på manuell behandling" }
            val message = ManuellBehandlingAvklartMessage(packet)
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

internal class AvklaringBehandletMessage(
    private val packet: JsonMessage,
) : HendelseMessage(packet) {
    private val hendelse
        get() =
            ManuellBehandlingAvklartHendelse(
                id,
                ident,
                behandlingId = packet["behandlingId"].asUUID(),
                behandlesManuelt = packet["@løsning"]["AvklaringManuellBehandling"].asBoolean(),
                avklaringer =
                    packet["vurderinger"].map {
                        ManuellBehandlingAvklartHendelse.Avklaring(it["type"].asText(), it["utfall"].asText(), it["begrunnelse"].asText())
                    },
            )
    override val ident get() = packet["ident"].asText()

    private companion object {
        private val logger = KotlinLogging.logger {}
    }

    override fun behandle(
        mediator: IMessageMediator,
        context: MessageContext,
    ) {
        withLoggingContext(hendelse.kontekstMap()) {
            logger.info { "Behandler avklaring på manuell vurdering" }
            // mediator.behandle(hendelse, this, context)
        }
    }
}
