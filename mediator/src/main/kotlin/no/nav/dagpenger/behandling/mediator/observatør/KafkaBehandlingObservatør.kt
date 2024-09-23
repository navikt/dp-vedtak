package no.nav.dagpenger.behandling.mediator.observatør

import com.github.navikt.tbd_libs.rapids_and_rivers.JsonMessage
import com.github.navikt.tbd_libs.rapids_and_rivers_api.RapidsConnection
import no.nav.dagpenger.behandling.modell.BehandlingObservatør.BehandlingEndretTilstand
import no.nav.dagpenger.behandling.modell.PersonObservatør

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
