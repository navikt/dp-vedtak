package no.nav.dagpenger.behandling

import mu.withLoggingContext
import no.nav.dagpenger.behandling.modell.BehandlingObservatør
import no.nav.dagpenger.behandling.modell.PersonObservatør
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext

internal class KafkaBehandlingObservatør(private val messageContext: MessageContext) : PersonObservatør {
    private companion object {
        val logger = mu.KotlinLogging.logger {}
    }

    override fun behandlingOpprettet(behandlingOpprettet: BehandlingObservatør.BehandlingOpprettet) {
        withLoggingContext(
            mapOf(
                "behandlingId" to behandlingOpprettet.behandlingId.toString(),
                "søknadId" to behandlingOpprettet.søknadId.toString(),
            ),
        ) {
            val event =
                JsonMessage.newMessage(
                    eventName = "behandling_opprettet",
                    mapOf(
                        "ident" to behandlingOpprettet.ident,
                        "behandlingId" to behandlingOpprettet.behandlingId,
                        "søknadId" to behandlingOpprettet.søknadId,
                    ),
                )
            messageContext.publish(behandlingOpprettet.ident, event.toJson())
            logger.info { "Sendt 'behandling_opprettet'" }
        }
    }

    override fun behandlingAvsluttet(behandlingAvsluttet: BehandlingObservatør.BehandlingAvsluttet) {
        withLoggingContext(
            mapOf(
                "behandlingId" to behandlingAvsluttet.behandlingId.toString(),
                "søknadId" to behandlingAvsluttet.søknadId.toString(),
            ),
        ) {
            val event =
                JsonMessage.newMessage(
                    "behandling_avsluttet",
                    mapOf(
                        "ident" to behandlingAvsluttet.ident,
                        "behandlingId" to behandlingAvsluttet.behandlingId,
                        "søknadId" to behandlingAvsluttet.søknadId,
                    ),
                )
            messageContext.publish(behandlingAvsluttet.ident, event.toJson())
            logger.info { "Sendt 'behandling_avsluttet'" }
        }
    }
}
