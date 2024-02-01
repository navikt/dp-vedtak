package no.nav.dagpenger.vedtak.modell.entitet

import java.math.BigDecimal
import java.math.RoundingMode

class Beløp private constructor(verdi: BigDecimal) : Comparable<Beløp> {
    private val verdi = verdi.setScale(antallDesimaler, RoundingMode.HALF_UP) // @todo: Setter avrunding som test.

    companion object {
        private val antallDesimaler = 2

        fun fra(sats: BigDecimal): Beløp {
            return Beløp(sats)
        }

        fun Iterable<Beløp>.summerBeløp() = sumOf { it.verdi }.beløp

        val Number.beløp get() = Beløp(BigDecimal.valueOf(this.toDouble()))
        val BigDecimal.beløp get() = Beløp(this)
    }

    fun <R> reflection(block: (BigDecimal) -> R) = block(verdi)

    override fun compareTo(other: Beløp): Int = verdi.compareTo(other.verdi)

    override fun equals(other: Any?) = other is Beløp && other.verdi == this.verdi

    override fun hashCode() = verdi.hashCode()

    infix operator fun plus(beløp: Beløp): Beløp = Beløp(this.verdi + beløp.verdi)

    infix operator fun minus(beløp: Beløp): Beløp = Beløp(this.verdi - beløp.verdi)

    infix operator fun times(beløp: Beløp): Beløp = Beløp(verdi * beløp.verdi)

    infix operator fun div(beløp: Beløp): Beløp = Beløp((verdi / beløp.verdi).setScale(antallDesimaler))

    infix operator fun div(timer: Timer): Beløp = this / timer.timer.beløp // @todo: Ikke eksponer "private" timer verdier

    infix operator fun times(faktor: Number): Beløp = Beløp(this.verdi * BigDecimal.valueOf(faktor.toDouble()))

    override fun toString(): String = verdi.toString()
}
