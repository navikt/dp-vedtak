package no.nav.dagpenger.vedtak.modell.rapportering

import no.nav.dagpenger.vedtak.modell.entitet.Periode
import no.nav.dagpenger.vedtak.modell.visitor.RapporteringsperiodeVisitor
import java.time.LocalDate
import java.util.SortedSet
import java.util.UUID

class Rapporteringsperiode(
    private val rapporteringsId: UUID,
    private val periode: Periode,
    dager: List<Rapporteringsdag>,
) : ClosedRange<LocalDate> by periode {
    constructor(rapporteringsId: UUID, periode: Periode) : this(rapporteringsId, periode, emptyList())

    private val dager: SortedSet<Rapporteringsdag> = dager.toSortedSet()

    fun leggTilDag(rapporteringsdag: Rapporteringsdag) {
        dager.add(rapporteringsdag)
    }

    fun accept(visitor: RapporteringsperiodeVisitor) {
        visitor.preVisitRapporteringsperiode(rapporteringsId, this)
        dager.forEach { it.accept(visitor) }
        visitor.postVisitRapporteringsperiode(rapporteringsId, this)
    }

    companion object {
        internal fun Iterable<Rapporteringsperiode>.merge(other: Rapporteringsperiode): List<Rapporteringsperiode> {
            val index = this.indexOfFirst { it.sammenfallerMed(other) }
            if (index == -1) return this.toMutableList().also { it.add(other) }
            return this.mapIndexed { i, rapporteringsperiode -> if (i == index) other else rapporteringsperiode }
        }
    }

    private fun sammenfallerMed(other: Rapporteringsperiode): Boolean = this.dager.first().sammenfallerMed(other.dager.first())
}
