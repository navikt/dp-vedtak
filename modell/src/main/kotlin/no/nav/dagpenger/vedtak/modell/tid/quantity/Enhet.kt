package no.nav.dagpenger.vedtak.modell.tid.quantity

import kotlin.math.absoluteValue

// Understands a specific metric
class Enhet {
    companion object {
        private val arbeidsdag = Enhet()
        private val arbeidsuke = Enhet(5, arbeidsdag)
        val Number.arbeidsdager get() = IntervallMengde(this, arbeidsdag)
        val Number.arbeidsuker get() = IntervallMengde(this, arbeidsuke)
    }

    private val baseEnhet: Enhet
    private val baseEnhetForhold: Double
    private val offset: Double

    private constructor() {
        baseEnhet = this
        baseEnhetForhold = 1.0
        offset = 0.0
    }

    private constructor(relativeRatio: Number, relativeUnit: Enhet) :
        this(relativeRatio, 0.0, relativeUnit)

    private constructor(relativeRatio: Number, offset: Number, relativeUnit: Enhet) {
        baseEnhet = relativeUnit.baseEnhet
        baseEnhetForhold = relativeRatio.toDouble() * relativeUnit.baseEnhetForhold
        this.offset = offset.toDouble()
    }

    internal fun convertedAmount(otherAmount: Double, other: Enhet): Double {
        require(this.isCompatible(other)) { "Incompatible Unit types" }
        return (otherAmount - other.offset) * other.baseEnhetForhold / this.baseEnhetForhold + this.offset
    }

    internal fun hashCode(amount: Double) =
        ((amount - offset) * baseEnhetForhold / IntervallMengde.DELTA).toLong().hashCode()

    internal fun isCompatible(other: Enhet) = this.baseEnhet == other.baseEnhet
}

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
