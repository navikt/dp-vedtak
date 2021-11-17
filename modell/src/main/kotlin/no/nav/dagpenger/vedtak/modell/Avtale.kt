package no.nav.dagpenger.vedtak.modell

internal class Avtale {

    val beregningsregler = listOf<Beregningsregel>()

    fun erAktiv() = true

    internal fun sats() {
        beregningsregler.filterIsInstance<SatsBeregningsregel>().last()
    }

}
