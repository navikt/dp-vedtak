package no.nav.dagpenger.vedtak.modell

import no.nav.dagpenger.vedtak.modell.beregningsregler.Beregningsregel
import no.nav.dagpenger.vedtak.modell.beregningsregler.SatsBeregningsregel
import no.nav.dagpenger.vedtak.modell.konto.Konto

typealias Mengde = Int

internal class Avtale {

    private val beregningsregler = mutableListOf<Beregningsregel>()
    private val kontoer = mutableMapOf<String, Konto>()

    internal fun erAktiv() = true

    fun sats() = beregningsregler.filterIsInstance<SatsBeregningsregel>().last().sats

    fun leggTilKonto(navn: String, konto: Konto) {
        kontoer[navn] = konto
    }

    internal fun endre() {
        TODO("Not yet implemented")
    }

    fun leggTilBeregningsregel(beregningsregel: Beregningsregel) {
        beregningsregler.add(beregningsregel)
    }

    fun finnBeregningsregel() = beregningsregler.last()

    fun balanse(konto: String) = kontoer[konto]?.balanse()
}
