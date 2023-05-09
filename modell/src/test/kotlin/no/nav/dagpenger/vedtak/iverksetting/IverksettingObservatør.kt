package no.nav.dagpenger.vedtak.iverksetting

internal class IverksettingObservatør : IverksettingObserver {
    val tilstander = mutableListOf<Iverksetting.Tilstand.TilstandNavn>().also {
        it.add(Iverksetting.Tilstand.TilstandNavn.Mottatt)
    }
    override fun iverksettingTilstandEndret(event: IverksettingObserver.IverksettingEndretTilstandEvent) {
        tilstander.add(event.gjeldendeTilstand)
    }
}