package no.nav.dagpenger.vedtak.modell

import no.nav.dagpenger.vedtak.modell.visitor.BeregnetTidslinjeVisitor

class BeregnetTidslinje(val beregnedeDager: List<BeregnetDag>) {
    fun accept(visitor: BeregnetTidslinjeVisitor) {
        visitor.preVisitBeregnetDager()
        beregnedeDager.forEach { it.accept(visitor) }
        visitor.postVisitBeregnetDager()
    }
}
