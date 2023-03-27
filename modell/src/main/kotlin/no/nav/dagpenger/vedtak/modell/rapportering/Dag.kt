package no.nav.dagpenger.vedtak.modell.rapportering

import no.nav.dagpenger.vedtak.modell.entitet.Periode
import no.nav.dagpenger.vedtak.modell.entitet.Timer
import no.nav.dagpenger.vedtak.modell.entitet.Timer.Companion.summer
import no.nav.dagpenger.vedtak.modell.entitet.Timer.Companion.timer
import no.nav.dagpenger.vedtak.modell.visitor.RapporteringsperiodeVisitor
import java.time.DayOfWeek
import java.time.LocalDate

sealed class Dag(internal val dato: LocalDate) : Comparable<LocalDate> {
    abstract fun accept(visitor: RapporteringsperiodeVisitor)
    abstract fun arbeidstimer(): Timer

    internal fun innenfor(periode: Periode) = dato in periode
    override fun compareTo(other: LocalDate): Int = this.dato.compareTo(other)
    companion object {
        internal fun fraværsdag(dato: LocalDate) = Fraværsdag(dato)

        internal fun arbeidsdag(dato: LocalDate, timer: Timer): Dag {
            return if (dato.erHelg()) {
                Helgedag(dato, timer)
            } else {
                Arbeidsdag(dato, timer)
            }
        }
        internal fun Collection<Dag>.summerArbeidstimer() = map(Dag::arbeidstimer).summer()

        private fun LocalDate.erHelg() = dayOfWeek in setOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)
    }
}

class Fraværsdag(dato: LocalDate) : Dag(dato) {
    override fun arbeidstimer(): Timer = 0.timer

    override fun accept(visitor: RapporteringsperiodeVisitor) {
        visitor.visitFraværsdag(this)
    }

    override fun toString(): String {
        return "Fraværsdag(dato=$dato,timer=${arbeidstimer()})"
    }
}

class Arbeidsdag(dato: LocalDate, private val timer: Timer) : Dag(dato) {
    override fun arbeidstimer(): Timer = timer
    override fun accept(visitor: RapporteringsperiodeVisitor) {
        visitor.visitArbeidsdag(this)
    }
    override fun toString(): String {
        return "Arbeidsdag(dato=$dato,timer=${arbeidstimer()})"
    }
}
class Helgedag(dato: LocalDate, private val timer: Timer) : Dag(dato) {
    override fun arbeidstimer(): Timer = timer
    override fun accept(visitor: RapporteringsperiodeVisitor) {
        visitor.visitHelgedag(this)
    }

    override fun toString(): String {
        return "Helgedag(dato=$dato,timer=${arbeidstimer()})"
    }
}
