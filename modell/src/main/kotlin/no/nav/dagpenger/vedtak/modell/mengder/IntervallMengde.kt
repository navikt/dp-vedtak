package no.nav.dagpenger.vedtak.modell.mengder

import kotlin.math.absoluteValue

// Understands a specific measurement
open class IntervallMengde internal constructor(amount: Number, protected val unit: Enhet) {
    protected val amount = amount.toDouble()

    companion object {
        internal const val DELTA = 0.00000001
    }

    override fun equals(other: Any?) = other is IntervallMengde && this.equals(other)

    private fun equals(other: IntervallMengde) =
        this.isCompatible(other) &&
            (this.amount - convertedAmount(other)).absoluteValue < DELTA

    private fun isCompatible(other: IntervallMengde) = this.unit.isCompatible(other.unit)

    override fun hashCode() = unit.hashCode(amount)

    protected fun convertedAmount(other: IntervallMengde) = this.unit.convertedAmount(other.amount, other.unit)

    operator fun unaryPlus() = this

    operator fun unaryMinus() = IntervallMengde(-amount, unit)

    operator fun plus(other: IntervallMengde) = IntervallMengde(this.amount + convertedAmount(other), unit)

    operator fun minus(other: IntervallMengde) = this + -other
}
