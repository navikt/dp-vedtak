package no.nav.dagpenger.vedtak.modell

import no.nav.dagpenger.vedtak.modell.visitor.BeregningHistorikkVisitor

class BeregningHistorikk {

    private val beregningTidslinjer = mutableListOf<BeregnetTidslinje>()
    private val gjeldende get() = beregningTidslinjer.lastOrNull()

    fun leggTilTidslinje(beregnetTidslinje: BeregnetTidslinje) {
        beregningTidslinjer.add(beregnetTidslinje)
    }

    fun accept(visitor: BeregningHistorikkVisitor) {
        visitor.preVisitBeregnetTidslinje()
        gjeldende?.accept(visitor)
        visitor.postVisitBeregnetTidslinje()
    }

    private class DagerIgjenVisitor(val beregningHistorikk: BeregningHistorikk) : BeregningHistorikkVisitor {

        var dagerIgjen = 0
    }
}
