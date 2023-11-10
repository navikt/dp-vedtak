package no.nav.dagpenger.vedtak.modell.rapportering

import no.nav.dagpenger.vedtak.modell.entitet.Timer
import no.nav.dagpenger.vedtak.modell.entitet.Timer.Companion.summer
import no.nav.dagpenger.vedtak.modell.visitor.RapporteringsdagVisitor
import java.time.DayOfWeek
import java.time.LocalDate

class Rapporteringsdag private constructor(
    private val dato: LocalDate,
    private val aktiviteter: List<Aktivitet>,
) : Comparable<Rapporteringsdag> {
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

    override fun compareTo(other: Rapporteringsdag) = eldsteDagFørst.compare(this, other)

    internal fun arbeidstimer(): Timer = aktiviteter.filterIsInstance<Arbeid>().map { it.timer }.summer()

    // @todo: Fag/juridisk - hva defineres som fravær som ikke spiser av stønadsperiode
    internal fun fravær() = aktiviteter.any { aktivitet -> aktivitet is Syk || aktivitet is Ferie }

    internal fun erHelg() = dato.dayOfWeek in setOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)

    internal fun sammenfallerMed(other: Rapporteringsdag) = this.dato == other.dato

    internal fun innenfor(periode: ClosedRange<LocalDate>) = this.dato in periode

    override fun toString(): String {
        return "Aktivitetsdag(dato=$dato, aktiviteter=$aktiviteter)"
    }

    fun accept(visitor: RapporteringsdagVisitor) {
        visitor.visitRapporteringsdag(this, aktiviteter)
    }

    companion object {
        fun opprett(
            dato: LocalDate,
            aktiviteter: List<Aktivitet>,
        ): Rapporteringsdag {
            return Rapporteringsdag(dato, aktiviteter)
        }

        internal val eldsteDagFørst = Comparator<Rapporteringsdag> { a, b -> a.dato.compareTo(b.dato) }
    }
}

sealed class Aktivitet(val timer: Timer, val type: AktivitetType) {
    enum class AktivitetType {
        Arbeid,
        Syk,
        Ferie,
    }

    override fun toString(): String {
        return "Aktivitet(timer=$timer, type=$type)"
    }
}

class Syk(timer: Timer) : Aktivitet(timer, AktivitetType.Syk)

class Arbeid(timer: Timer) : Aktivitet(timer, AktivitetType.Arbeid)

class Ferie(timer: Timer) : Aktivitet(timer, AktivitetType.Ferie)
