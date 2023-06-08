package no.nav.dagpenger.vedtak.modell.utbetaling

import no.nav.dagpenger.vedtak.modell.entitet.Beløp
import no.nav.dagpenger.vedtak.modell.entitet.Beløp.Companion.beløp
import no.nav.dagpenger.vedtak.modell.entitet.Beløp.Companion.summerBeløp
import java.time.LocalDate

sealed class LøpendeRettighetDag(private val dato: LocalDate, private val beløp: Beløp) {

    internal companion object {
        fun Collection<LøpendeRettighetDag>.summer(): Beløp = this.map { it.beløp }.summerBeløp()
    }
}
class BeregnetBeløpDag(dato: LocalDate, beløp: Beløp) : LøpendeRettighetDag(dato, beløp)
class NullBeløpDag(dato: LocalDate) : LøpendeRettighetDag(dato, 0.beløp)
