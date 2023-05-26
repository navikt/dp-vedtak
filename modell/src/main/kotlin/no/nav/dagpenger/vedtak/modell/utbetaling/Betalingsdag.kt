package no.nav.dagpenger.vedtak.modell.utbetaling

import no.nav.dagpenger.vedtak.modell.entitet.Beløp
import no.nav.dagpenger.vedtak.modell.entitet.Beløp.Companion.beløp
import no.nav.dagpenger.vedtak.modell.entitet.Beløp.Companion.summerBeløp
import java.time.LocalDate

sealed class Betalingsdag(private val dato: LocalDate, private val beløp: Beløp) {

    internal companion object {
        fun Collection<Betalingsdag>.summer(): Beløp = this.map { it.beløp }.summerBeløp()
    }
}
class Utbetalingsdag(dato: LocalDate, beløp: Beløp) : Betalingsdag(dato, beløp)
class IkkeUtbetalingsdag(dato: LocalDate) : Betalingsdag(dato, 0.beløp)
