package no.nav.dagpenger.vedtak.modell.visitor

interface BeregningHistorikkVisitor : BeregnetTidslinjeVisitor {

    fun preVisitBeregnetTidslinje() {}
    fun postVisitBeregnetTidslinje() {}
}
