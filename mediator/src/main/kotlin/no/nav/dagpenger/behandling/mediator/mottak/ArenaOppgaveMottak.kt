package no.nav.dagpenger.behandling.mediator.mottak

import com.github.navikt.tbd_libs.rapids_and_rivers.JsonMessage
import com.github.navikt.tbd_libs.rapids_and_rivers.River
import com.github.navikt.tbd_libs.rapids_and_rivers_api.MessageContext
import com.github.navikt.tbd_libs.rapids_and_rivers_api.RapidsConnection
import io.opentelemetry.instrumentation.annotations.WithSpan
import mu.KotlinLogging
import mu.withLoggingContext

internal class ArenaOppgaveMottak(
    rapidsConnection: RapidsConnection,
) : River.PacketListener {
    init {
        River(rapidsConnection)
            .apply {
                validate { it.requireKey("op_type", "pos") }
                // validate { it.demandKey("oppgave_logg_id") }
                // validate { it.requireKey("sak_id") }
            }.register(this)
    }

    @WithSpan
    override fun onPacket(
        packet: JsonMessage,
        context: MessageContext,
    ) {
        val sakId = packet["sak_id"].toString()
        withLoggingContext("sakId" to sakId) {
            logger.info { "Mottok oppgave fra Arena" }
            sikkerlogg.info { "Mottok oppgave fra Arena. Pakke=${packet.toJson()}" }
        }
    }

    private companion object {
        private val logger = KotlinLogging.logger {}
        private val sikkerlogg = KotlinLogging.logger("tjenestekall.ArenaOppgaveMottak")
    }
}
