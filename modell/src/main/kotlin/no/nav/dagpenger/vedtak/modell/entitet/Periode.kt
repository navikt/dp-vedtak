package no.nav.dagpenger.vedtak.modell.entitet

import no.nav.dagpenger.vedtak.modell.rapportering.Rapporteringsdag
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class Periode(private val fomDato: LocalDate, private val tomDato: LocalDate) : ClosedRange<LocalDate>, Iterable<LocalDate> {
    override val endInclusive: LocalDate
        get() = tomDato
    override val start: LocalDate
        get() = fomDato

    init {
        require(start <= endInclusive) {
            "fomDato $start kan ikke være etter tomDato $endInclusive"
        }
    }

    override fun iterator(): Iterator<LocalDate> = object : Iterator<LocalDate> {
        private var currentDate: LocalDate = start

        override fun hasNext() = endInclusive >= currentDate

        override fun next() =
            currentDate.also { currentDate = it.plusDays(1) }
    }

    operator fun contains(rapporteringsdag: Rapporteringsdag): Boolean = rapporteringsdag.innenfor(this)

    infix operator fun plus(annen: Periode): Periode {
        return Periode(minOf(this.start, annen.start), maxOf(this.endInclusive, annen.endInclusive))
    }

    override fun toString(): String {
        return start.format(formatter) + " til " + endInclusive.format(formatter)
    }

    companion object {
        private val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
        internal infix fun LocalDate.til(tom: LocalDate) = Periode(this, tom)
    }
}
