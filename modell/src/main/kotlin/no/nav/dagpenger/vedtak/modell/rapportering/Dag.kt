package no.nav.dagpenger.vedtak.modell.rapportering

import no.nav.dagpenger.vedtak.modell.entitet.Periode
import no.nav.dagpenger.vedtak.modell.entitet.Timer
import no.nav.dagpenger.vedtak.modell.entitet.Timer.Companion.summer
import no.nav.dagpenger.vedtak.modell.visitor.DagVisitor
import java.time.DayOfWeek
import java.time.LocalDate

class Dag private constructor(
    private val dato: LocalDate,
    private val aktiviteter: List<Aktivitet>,
) : Comparable<Dag> {
    init {
        /**
         * NB! Databasen støtter kun 1 aktivitet av samme type
         *
         */

        require(aktiviteter.isEmpty() || aktiviteter.size == 1) {
            "Støtter bare 1 aktivitet per dag pt."
        }
    }
    fun dato() = dato
    override fun compareTo(other: Dag) = eldsteDagFørst.compare(this, other)
    internal fun arbeidstimer(): Timer = aktiviteter.filterIsInstance<Arbeid>().map { it.timer }.summer()
    internal fun fravær() = aktiviteter.any { aktivitet -> aktivitet is Syk || aktivitet is Ferie } // @todo: Fag/juridisk - hva defineres som fravær som ikke spiser av stønadsperiode
    internal fun erHelg() = dato.dayOfWeek in setOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)
    internal fun sammenfallerMed(other: Dag) = this.dato == other.dato
    internal fun innenfor(periode: Periode) = this.dato in periode
    override fun toString(): String {
        return "Aktivitetsdag(dato=$dato, aktiviteter=$aktiviteter)"
    }

    fun accept(visitor: DagVisitor) {
        visitor.visitDag(this, aktiviteter)
    }

    companion object {
        fun opprett(dato: LocalDate, aktiviteter: List<Aktivitet>): Dag {
            return Dag(dato, aktiviteter)
        }
        private val eldsteDagFørst = Comparator<Dag> { a, b -> a.dato.compareTo(b.dato) }
    }
}

sealed class Aktivitet(val timer: Timer, val type: AktivitetType) {
    enum class AktivitetType {
        Arbeid, Syk, Ferie
    }

    override fun toString(): String {
        return "Aktivitet(timer=$timer, type=$type)"
    }
}
class Syk(timer: Timer) : Aktivitet(timer, AktivitetType.Syk)
class Arbeid(timer: Timer) : Aktivitet(timer, AktivitetType.Arbeid)
class Ferie(timer: Timer) : Aktivitet(timer, AktivitetType.Ferie)
