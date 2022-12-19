package no.nav.dagpenger.vedtak.modell.visitor

interface BeregnetTidslinjeVisitor : BeregnetDagVisitor {

    fun preVisitBeregnetDager() {}
    fun postVisitBeregnetDager() {}
}
