package no.nav.dagpenger.behandling.mediator.melding

import java.util.UUID

internal interface HendelseRepository {
    fun lagreMelding(
        hendelseMessage: HendelseMessage,
        ident: String,
        id: UUID,
        toJson: String,
    )

    fun markerSomBehandlet(meldingId: UUID): Int

    fun erBehandlet(meldingId: UUID): Boolean
}
