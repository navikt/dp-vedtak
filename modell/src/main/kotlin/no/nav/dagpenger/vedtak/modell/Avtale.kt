package no.nav.dagpenger.vedtak.modell

import no.nav.dagpenger.vedtak.modell.beregningsregler.Beregningsregel
import no.nav.dagpenger.vedtak.modell.beregningsregler.SatsBeregningsregel
import no.nav.dagpenger.vedtak.modell.konto.Konto
import no.nav.dagpenger.vedtak.modell.konto.StønadsperiodeKonto

class Avtale {

    private val beregningsregler = mutableListOf<Beregningsregel>()
    private val dagpengePeriodeKonto: Konto = StønadsperiodeKonto()

    internal fun erAktiv() = true

    fun sats() = beregningsregler.filterIsInstance<SatsBeregningsregel>().last().sats

    internal fun endre() {
        TODO("Not yet implemented")
    }

    fun leggTilBeregningsregel(beregningsregel: Beregningsregel) {
        beregningsregler.add(beregningsregel)
    }
}
