package no.nav.dagpenger.vedtak.modell

import java.math.BigDecimal

class Beløp private constructor(verdi: Number) : Comparable<Beløp> {

    private val verdi: Double = verdi.toDouble()
    internal companion object {
        fun fra(sats: BigDecimal): Beløp {
            return Beløp(sats)
        }

        fun Iterable<Beløp>.summerBeløp() = sumOf { it.verdi }.beløp

        val Number.beløp get() = Beløp(this)
    }
    override fun compareTo(other: Beløp): Int = verdi.compareTo(other.verdi)
    override fun equals(other: Any?) = other is Beløp && other.verdi == this.verdi
    override fun hashCode() = verdi.hashCode()
    infix operator fun plus(beløp: Beløp): Beløp = Beløp(this.verdi + beløp.verdi)
    infix operator fun minus(beløp: Beløp): Beløp = Beløp(this.verdi - beløp.verdi)
    infix operator fun times(beløp: Beløp): Beløp = Beløp(verdi * beløp.verdi)
    infix operator fun div(beløp: Beløp): Beløp = Beløp(verdi / beløp.verdi)
    override fun toString(): String = verdi.toString()
}
