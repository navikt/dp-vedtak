package no.nav.dagpenger.vedtak.modell.rapportering

import no.nav.dagpenger.vedtak.modell.visitor.RapporteringsperiodeVisitor
import java.util.SortedSet
import java.util.UUID

class Rapporteringsperiode(private val rapporteringsId: UUID, dager: List<Dag>) : Iterable<Dag> {
    constructor(rapporteringsId: UUID) : this(rapporteringsId, emptyList())
    private val dager: SortedSet<Dag> = dager.toSortedSet()

    fun leggTilDag(dag: Dag) {
        dager.add(dag)
    }

    fun accept(visitor: RapporteringsperiodeVisitor) {
        visitor.preVisitRapporteringsperiode(rapporteringsId, dager.first().dato(), dager.last().dato())
        dager.forEach { visitor.visitdag(it) }
        visitor.postVisitRapporteringsperiode(rapporteringsId, dager.first().dato(), dager.last().dato())
    }

    override fun iterator(): Iterator<Dag> {
        return dager.iterator()
    }

    companion object {
        internal fun Iterable<Rapporteringsperiode>.merge(other: Rapporteringsperiode): List<Rapporteringsperiode> {
            val index = this.indexOfFirst { it.sammenfallerMed(other) }
            if (index == -1) return this.toMutableList().also { it.add(other) }
            return this.mapIndexed { i, rapporteringsperiode -> if (i == index) other else rapporteringsperiode }
        }
    }

    private fun sammenfallerMed(other: Rapporteringsperiode): Boolean =
        this.dager.first().sammenfallerMed(other.dager.first())
}
