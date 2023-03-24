package no.nav.dagpenger.vedtak.modell.rapportering

import no.nav.dagpenger.vedtak.modell.visitor.RapporteringsperiodeVisitor
import java.util.UUID

class Rapporteringsperiode(internal val rapporteringsId: UUID, dager: List<Dag>) : Iterable<Dag> {
    constructor(rapporteringsId: UUID) : this(rapporteringsId, emptyList())
    private val dager = dager.toMutableList()

    fun leggTilDag(dag: Dag) {
        dager.add(dag)
    }

    fun accept(visitor: RapporteringsperiodeVisitor) {
        visitor.preVisitRapporteringPeriode(this)
        dager.forEach { it.accept(visitor) }
        visitor.postVisitRapporteringPeriode(this)
    }

    override fun iterator(): Iterator<Dag> {
        return dager.iterator()
    }
}
