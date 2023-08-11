package no.nav.dagpenger.vedtak.modell.utbetaling

import no.nav.dagpenger.vedtak.modell.entitet.Beløp
import no.nav.dagpenger.vedtak.modell.entitet.Beløp.Companion.summerBeløp
import java.time.LocalDate

class Utbetalingsdag(val dato: LocalDate, val beløp: Beløp) { // TODO: Sett på private og bruk visitor

    internal companion object {
        fun Collection<Utbetalingsdag>.summer(): Beløp = this.map { it.beløp }.summerBeløp()
    }

    override fun equals(other: Any?) =
        other is Utbetalingsdag && this.dato == other.dato && this.beløp == other.beløp

    override fun hashCode(): Int {
        var result = dato.hashCode()
        result = 31 * result + beløp.hashCode()
        return result
    }

    override fun toString() = "Utbetalingsdag(dato: $dato, beløp: $beløp)"
}
