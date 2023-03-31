package no.nav.dagpenger.vedtak.modell.utbetaling

import no.nav.dagpenger.vedtak.modell.Beløp
import no.nav.dagpenger.vedtak.modell.Beløp.Companion.beløp
import no.nav.dagpenger.vedtak.modell.Beløp.Companion.summerBeløp
import java.time.LocalDate

internal sealed class Betalingsdag(private val dato: LocalDate, private val beløp: Beløp) {

    internal companion object {
        fun Collection<Betalingsdag>.summer(): Beløp = this.map { it.beløp }.summerBeløp()
    }
}
internal class Utbetalingsdag(dato: LocalDate, beløp: Beløp) : Betalingsdag(dato, beløp)
internal class IkkeUtbetalingsdag(dato: LocalDate) : Betalingsdag(dato, 0.beløp)
