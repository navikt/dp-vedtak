package no.nav.dagpenger.behandling.modell.hendelser

import java.util.UUID

class AvklaringBehandletHendelse(
    meldingsreferanseId: UUID,
    ident: String,
    val behandlingId: UUID,
    val behandlesManuelt: Boolean,
    val avklaringer: List<Avklaring>,
) : PersonHendelse(meldingsreferanseId, ident) {
    data class Avklaring(
        val type: String,
        val utfall: String,
        val begrunnelse: String,
    )
}
