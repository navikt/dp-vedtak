package no.nav.dagpenger.vedtak.mediator.melding

import java.util.UUID

internal interface HendelseRepository {
    fun lagreMelding(hendelseMessage: HendelseMessage, ident: String, id: UUID, toJson: String)
    fun markerSomBehandlet(meldingId: UUID)

    fun hentMottatte(): List<HendelseMessage>
    fun hentBehandlede(): List<HendelseMessage>
}
