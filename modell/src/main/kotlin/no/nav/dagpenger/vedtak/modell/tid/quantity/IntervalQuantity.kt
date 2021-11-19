package no.nav.dagpenger.vedtak.modell.tid.quantity

import kotlin.math.absoluteValue

// Understands a specific measurement
open class IntervalQuantity internal constructor(amount: Number, private val unit: Unitt) : Orderable<IntervalQuantity> {
    private val epsilon = 0.000000001
    internal val amount = amount.toDouble()

    override fun equals(other: Any?) = this === other || other is IntervalQuantity && this.equals(other)

    private fun equals(other: IntervalQuantity) = this.isCompatible(other) &&
            (this.amount - convertedAmount(other)).absoluteValue < epsilon

    private fun isCompatible(other: IntervalQuantity) = this.unit.isCompatible(other.unit)

    internal fun convertedAmount(other: IntervalQuantity) = this.unit.convertedAmount(other.amount, other.unit)

    override fun hashCode() = unit.hashCode(amount)

    override fun isBetterThan(other: IntervalQuantity) = this.amount > convertedAmount(other)
}