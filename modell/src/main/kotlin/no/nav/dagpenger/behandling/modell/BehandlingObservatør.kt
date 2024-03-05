package no.nav.dagpenger.behandling.modell

import java.util.UUID

interface BehandlingObservatør {
    fun behandlingOpprettet(behandlingOpprettet: BehandlingOpprettet) {}

    fun forslagTilVedtak(forslagTilVedtak: ForslagTilVedtak)

    data class BehandlingOpprettet(
        val ident: String,
        val behandlingId: UUID,
        val søknadId: UUID,
    )

    data class ForslagTilVedtak(
        val ident: String,
        val behandlingId: UUID,
        val søknadId: UUID,
    )
}
