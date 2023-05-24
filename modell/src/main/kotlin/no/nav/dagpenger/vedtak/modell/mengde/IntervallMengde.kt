package no.nav.dagpenger.vedtak.modell.mengde

import kotlin.math.absoluteValue

// Forstår ulike mengder som kan måles som intervaller, som f.eks. temperatur
open class IntervallMengde internal constructor(mengde: Number, protected val enhet: Enhet) : Comparable<IntervallMengde> {
    protected val amount = mengde.toDouble()

    companion object {
        internal const val DELTA = 0.00000001
    }

    override fun equals(other: Any?) = other is IntervallMengde && this.equals(other)

    private fun equals(other: IntervallMengde) =
        this.isCompatible(other) &&
            (this.amount - convertedAmount(other)).absoluteValue < DELTA

    private fun isCompatible(other: IntervallMengde) = this.enhet.isCompatible(other.enhet)

    override fun hashCode() = enhet.hashCode(amount)

    protected fun convertedAmount(other: IntervallMengde) = enhet.convertedAmount(other.amount, other.enhet)

    override fun toString(): String {
        return "Mengde(enhet=${enhet.javaClass.simpleName}, amount=$amount)"
    }
    override fun compareTo(other: IntervallMengde): Int {
        require(this.enhet.isCompatible(other.enhet)) { "Kan bare sammenligne med samme enhet." }
        val konvertertMengde = this.convertedAmount(other)
        return when {
            this.amount == konvertertMengde -> 0
            this.amount < konvertertMengde -> -1
            this.amount > konvertertMengde -> 1
            else -> throw IllegalArgumentException("Kan ikke sammenligne $this mot $other")
        }
    }
}
