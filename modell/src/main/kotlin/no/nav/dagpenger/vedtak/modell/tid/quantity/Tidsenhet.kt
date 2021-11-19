package no.nav.dagpenger.vedtak.modell.tid.quantity

// Understands a specific metric
class Tidsenhet {
    companion object {


        internal val enhet = Tidsenhet()
        internal val gange = Tidsenhet(1, enhet)
        internal val prosent = Tidsenhet(0.01, enhet)

        internal val day = Tidsenhet()
        internal val week = Tidsenhet(5, day)
    }
    private val baseEnhet: Tidsenhet
    private val baseEnhetRate: Double
    private val offset: Double

    private constructor() {
        baseEnhet = this
        baseEnhetRate = 1.0
        offset = 0.0
    }

    private constructor(relativeRatio: Number, relativeUnit: Tidsenhet) :
                this(relativeRatio, 0.0, relativeUnit)

    private constructor(relativeRatio: Number, offset: Number, relativeUnit: Tidsenhet) {
        baseEnhet = relativeUnit.baseEnhet
        baseEnhetRate = relativeRatio.toDouble() * relativeUnit.baseEnhetRate
        this.offset = offset.toDouble()
    }

    internal fun convertedAmount(otherAmount: Double, other: Tidsenhet): Double {
        require(this.isCompatible(other)) { "Incompatible Unit types" }
        return (otherAmount - other.offset) * other.baseEnhetRate / this.baseEnhetRate + this.offset
    }

    internal fun hashCode(amount: Double) = ((amount - offset) * baseEnhetRate).hashCode()

    internal fun isCompatible(other: Tidsenhet) = this.baseEnhet == other.baseEnhet

}

val Number.dager get(): RatioQuantity = SpecificQuantity(this, Tidsenhet.day)
val Number.uker get(): RatioQuantity = SpecificQuantity(this, Tidsenhet.week)