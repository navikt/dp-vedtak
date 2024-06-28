package no.nav.dagpenger.behandling.modell.hendelser

import java.util.UUID

class AvklaringIkkeRelevantHendelse(
    meldingsreferanseId: UUID,
    ident: String,
    val avklaringId: UUID,
    val kode: String,
    override val behandlingId: UUID,
    val utfall: String,
) : PersonHendelse(meldingsreferanseId, ident),
    BehandlingHendelse {
    override fun kontekstMap(): Map<String, String> =
        mapOf(
            "avklaringId" to avklaringId.toString(),
            "kode" to kode,
        )
}
