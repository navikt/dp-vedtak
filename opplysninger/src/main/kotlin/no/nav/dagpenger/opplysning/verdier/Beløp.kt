package no.nav.dagpenger.opplysning.verdier

import org.javamoney.moneta.Money
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode
import javax.money.Monetary
import javax.money.MonetaryContextBuilder

class Beløp private constructor(
    private val verdi: Money,
) : Comparable<Beløp> {
    constructor(verdi: Double) : this(BigDecimal.valueOf(verdi))
    constructor(verdi: BigDecimal) : this(
        Money.of(
            verdi,
            Monetary.getCurrency("NOK"),
            MonetaryContextBuilder
                .of()
                .set(
                    MathContext(
                        20,
                        RoundingMode.HALF_UP,
                    ),
                ).build(),
        ),
    )

    operator fun div(faktor: Double): Beløp = Beløp(verdi.divide(faktor))

    operator fun times(faktor: Double): Beløp = Beløp(verdi.multiply(faktor))

    override fun compareTo(other: Beløp): Int = verdi.compareTo(other.verdi)

    override fun equals(other: Any?): Boolean = other is Beløp && verdi == other.verdi

    override fun hashCode(): Int = verdi.hashCode()

    override fun toString(): String = verdi.toString()
}
