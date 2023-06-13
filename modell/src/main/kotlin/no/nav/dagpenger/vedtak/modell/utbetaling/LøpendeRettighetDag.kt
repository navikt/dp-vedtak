package no.nav.dagpenger.vedtak.modell.utbetaling

import no.nav.dagpenger.vedtak.modell.entitet.Beløp
import no.nav.dagpenger.vedtak.modell.entitet.Beløp.Companion.beløp
import no.nav.dagpenger.vedtak.modell.entitet.Beløp.Companion.summerBeløp
import java.time.LocalDate

sealed class LøpendeRettighetDag(val dato: LocalDate, val beløp: Beløp) { // TODO: Sett på private og bruk visitor

    internal companion object {
        fun Collection<LøpendeRettighetDag>.summer(): Beløp = this.map { it.beløp }.summerBeløp()
    }

    override fun equals(other: Any?) =
        other is LøpendeRettighetDag && this.dato == other.dato && this.beløp == other.beløp

    override fun hashCode(): Int {
        var result = dato.hashCode()
        result = 31 * result + beløp.hashCode()
        return result
    }
}

class BeregnetBeløpDag(dato: LocalDate, beløp: Beløp) : LøpendeRettighetDag(dato, beløp)
class NullBeløpDag(dato: LocalDate) : LøpendeRettighetDag(dato, 0.beløp)
