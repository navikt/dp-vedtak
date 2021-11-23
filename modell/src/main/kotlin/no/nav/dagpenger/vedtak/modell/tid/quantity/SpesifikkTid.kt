package no.nav.dagpenger.vedtak.modell.tid.quantity

// Understands a specific measurement
/*
internal class SpesifikkTid internal constructor(amount: Number, private val unit: Tidsenhet)
    : IntervalQuantity(amount, unit), RatioQuantity {

    internal fun zero() = SpesifikkTid(0, unit)

    override operator fun unaryPlus() = this

    override operator fun unaryMinus() = SpesifikkTid(-amount, unit)

    override operator fun plus(other: RatioQuantity)
            = SpesifikkTid(this.amount + convertedAmountOrZero(other), this.unit)

    override operator fun minus(other: RatioQuantity) = this + -other

    override fun equals(other: Any?) =
        this === other ||
                other is UniversalZero && super.equals(this.zero()) ||
                super.equals(other)

    override fun hashCode(): Int {
        return if (amount == 0.0) UniversalZero.hashCode() else super.hashCode()
    }

    override fun compareTo(other: RatioQuantity) = this.amount.compareTo(convertedAmountOrZero(other))

    private fun convertedAmountOrZero(other: RatioQuantity) =
        if(other is SpesifikkTid) convertedAmount(other) else 0.0
}

interface RatioQuantity : Comparable<RatioQuantity> {
    companion object {
        val zero: RatioQuantity = UniversalZero
    }
    operator fun unaryPlus(): RatioQuantity
    operator fun unaryMinus(): RatioQuantity
    operator fun plus(other: RatioQuantity): RatioQuantity
    operator fun minus(other: RatioQuantity): RatioQuantity
}

internal object UniversalZero: RatioQuantity {
    override fun unaryPlus() = this

    override fun unaryMinus() = this

    override fun plus(other: RatioQuantity) = other

    override fun minus(other: RatioQuantity) = other.unaryMinus()

    override fun equals(other: Any?): Boolean {
        return this === other || other is RatioQuantity && other.equals(this)
    }

    override fun compareTo(other: RatioQuantity) =
        if (other is SpesifikkTid) other.zero().compareTo(other) else 0
} */
