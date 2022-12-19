package no.nav.dagpenger.vedtak.modell

import no.nav.dagpenger.vedtak.modell.visitor.BeregningHistorikkVisitor

class BeregningHistorikk {

    val beregningTidslinjer = mutableListOf<BeregnetTidslinje>()

    fun leggTilTidslinje(beregnetTidslinje: BeregnetTidslinje) {
        beregningTidslinjer.add(beregnetTidslinje)
    }

    fun accept(visitor: BeregningHistorikkVisitor) {
        visitor.preVisitBeregnetTidslinje()
        beregningTidslinjer.forEach { it.accept(visitor) }
        visitor.postVisitBeregnetTidslinje()
    }

    private class DagerIgjenVisitor(val beregningHistorikk: BeregningHistorikk) : BeregningHistorikkVisitor {

        var dagerIgjen = 0
    }
}
