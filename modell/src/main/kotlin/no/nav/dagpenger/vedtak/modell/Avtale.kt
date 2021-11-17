package no.nav.dagpenger.vedtak.modell

import no.nav.dagpenger.vedtak.modell.beregningsregler.Beregningsregel
import no.nav.dagpenger.vedtak.modell.beregningsregler.SatsBeregningsregel

class Avtale {

    private val beregningsregler = mutableListOf<Beregningsregel>()

    fun erAktiv() = true

    internal fun sats() = beregningsregler.filterIsInstance<SatsBeregningsregel>().last().sats

    internal fun endre() {
        TODO("Not yet implemented")
    }

    fun leggTilBeregningsregel(beregningsregel: Beregningsregel) {
        beregningsregler.add(beregningsregel)
    }
}
