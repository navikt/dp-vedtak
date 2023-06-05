package no.nav.dagpenger.vedtak.modell.rapportering

import no.nav.dagpenger.vedtak.modell.visitor.RapporteringsperiodeVisitor
import java.util.SortedSet
import java.util.UUID

class Rapporteringsperiode(internal val rapporteringsId: UUID, dager: List<Dag>) : Iterable<Dag> {
    constructor(rapporteringsId: UUID) : this(rapporteringsId, emptyList())
    private val dager: SortedSet<Dag> = dager.toSortedSet()

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

    companion object {
        internal fun Iterable<Rapporteringsperiode>.merge(other: Rapporteringsperiode): List<Rapporteringsperiode> {
            val index = this.indexOfFirst { it.sammenfallerMed(other) }
            if (index == -1) return this.toMutableList().also { it.add(other) }
            return this.mapIndexed { i, meldeperiode -> if (i == index) other else meldeperiode }
        }
    }

    private fun sammenfallerMed(other: Rapporteringsperiode): Boolean =
        this.dager.first().sammenfallerMed(other.dager.first())
}
