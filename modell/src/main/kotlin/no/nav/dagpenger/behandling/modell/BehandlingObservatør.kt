package no.nav.dagpenger.behandling.modell

import java.util.UUID

interface BehandlingObservatør {
    fun behandlingOpprettet(behandlingOpprettet: BehandlingEvent.Opprettet) {}

    fun forslagTilVedtak(forslagTilVedtak: BehandlingEvent.ForslagTilVedtak)

    sealed class BehandlingEvent {
        data class Opprettet(
            val ident: String,
            val behandlingId: UUID,
            val søknadId: UUID,
        ) : BehandlingEvent()

        data class ForslagTilVedtak(
            val ident: String,
            val behandlingId: UUID,
            val søknadId: UUID,
        ) : BehandlingEvent()
    }
}
