package no.nav.dagpenger.vedtak.iverksetting

import java.util.UUID

interface IverksettingObserver {
    fun iverksettingTilstandEndret(event: IverksettingEndretTilstandEvent) {}

    data class IverksettingEndretTilstandEvent(
        val iversettingId: UUID,
        val vedtakId: UUID,
        val gjeldendeTilstand: Iverksetting.Tilstand.TilstandNavn,
        val forrigeTilstand: Iverksetting.Tilstand.TilstandNavn,
    )
}
