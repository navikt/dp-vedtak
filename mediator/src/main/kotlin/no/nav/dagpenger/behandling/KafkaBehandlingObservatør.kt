package no.nav.dagpenger.behandling

import mu.withLoggingContext
import no.nav.dagpenger.behandling.modell.BehandlingObservatør.BehandlingEvent
import no.nav.dagpenger.behandling.modell.BehandlingObservatørAdapter
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext

internal class KafkaBehandlingObservatør(private val messageContext: MessageContext) : BehandlingObservatørAdapter {
    private companion object {
        val logger = mu.KotlinLogging.logger {}
    }

    override fun behandlingOpprettet(behandlingOpprettet: BehandlingEvent.Opprettet) {
        withLoggingContext(
            mapOf(
                "behandlingId" to behandlingOpprettet.behandlingId.toString(),
                "søknadId" to behandlingOpprettet.søknadId.toString(),
            ),
        ) {
            sendEvent(
                "behandling_opprettet",
                mapOf(
                    "ident" to behandlingOpprettet.ident,
                    "behandlingId" to behandlingOpprettet.behandlingId,
                    "søknadId" to behandlingOpprettet.søknadId,
                ),
            )
        }
    }

    override fun forslagTilVedtak(forslagTilVedtak: BehandlingEvent.ForslagTilVedtak) {
        withLoggingContext(
            mapOf(
                "behandlingId" to forslagTilVedtak.behandlingId.toString(),
                "søknadId" to forslagTilVedtak.søknadId.toString(),
            ),
        ) {
            sendEvent(
                "forslag_til_vedtak",
                mapOf(
                    "ident" to forslagTilVedtak.ident,
                    "behandlingId" to forslagTilVedtak.behandlingId,
                    "søknadId" to forslagTilVedtak.søknadId,
                ),
            )
        }
    }

    private fun sendEvent(
        eventName: String,
        data: Map<String, Any>,
    ) {
        val event = JsonMessage.newMessage(eventName, data)
        messageContext.publish(event.toJson())
        logger.info { "Sendt '$eventName'" }
    }
}
