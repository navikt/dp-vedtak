package no.nav.dagpenger.behandling.mediator.observatør

import no.nav.dagpenger.behandling.modell.BehandlingObservatør.BehandlingEndretTilstand
import no.nav.dagpenger.behandling.modell.PersonObservatør
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.RapidsConnection

class KafkaBehandlingObservatør(
    private val rapid: RapidsConnection,
) : PersonObservatør {
    override fun endretTilstand(event: PersonObservatør.PersonEvent<BehandlingEndretTilstand>) {
        rapid.publish(event.ident, event.toJson())
    }

    private fun PersonObservatør.PersonEvent<BehandlingEndretTilstand>.toJson() =
        JsonMessage
            .newMessage(
                "behandling_endret_tilstand",
                mapOf(
                    "ident" to ident,
                    "behandlingId" to wrappedEvent.behandlingId.toString(),
                    "forrigeTilstand" to wrappedEvent.forrigeTilstand.name,
                    "gjeldendeTilstand" to wrappedEvent.gjeldendeTilstand.name,
                    "forventetFerdig" to wrappedEvent.forventetFerdig.toString(),
                    "tidBrukt" to wrappedEvent.tidBrukt.toString(),
                ),
            ).toJson()
}
