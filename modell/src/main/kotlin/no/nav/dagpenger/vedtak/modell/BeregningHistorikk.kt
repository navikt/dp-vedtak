package no.nav.dagpenger.vedtak.modell

class BeregningHistorikk {

    val beregningTidslinjer = mutableListOf<BeregnetTidslinje>()

    fun leggTilTidslinje(beregnetTidslinje: BeregnetTidslinje) {
        beregningTidslinjer.add(beregnetTidslinje)
    }
}
