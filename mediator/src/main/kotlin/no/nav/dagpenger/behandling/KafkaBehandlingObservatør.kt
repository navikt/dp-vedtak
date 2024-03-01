package no.nav.dagpenger.behandling

import no.nav.dagpenger.behandling.modell.BehandlingObservatør
import no.nav.dagpenger.behandling.modell.PersonObservatør
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext

internal class KafkaBehandlingObservatør(private val messageContext: MessageContext) : PersonObservatør {
    override fun behandlingOpprettet(behandlingOpprettet: BehandlingObservatør.BehandlingOpprettet) {
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
    }

    override fun behandlingAvsluttet(behandlingAvsluttet: BehandlingObservatør.BehandlingAvsluttet) {
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
    }
}
