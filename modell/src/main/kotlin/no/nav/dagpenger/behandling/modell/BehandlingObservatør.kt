package no.nav.dagpenger.behandling.modell

import java.util.UUID

interface BehandlingObservatør {
    fun behandlingOpprettet(behandlingOpprettet: BehandlingOpprettet) {}

    data class BehandlingOpprettet(
        val ident: String,
        val behandlingId: UUID,
        val søknadId: UUID,
    )
}
