package no.nav.dagpenger.vedtak.modell

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
