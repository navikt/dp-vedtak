package no.nav.dagpenger.vedtak.modell.tid.quantity

// Understands a specific metric
class Tidsenhet {
    companion object {
        internal val arbeidsdag = Tidsenhet()
        internal val arbeidsuke = Tidsenhet(5, arbeidsdag)
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
    internal fun isCompatible(other: Tidsenhet) = this.baseEnhet == other.baseEnhet

}

internal class SpesifikkTid(private val antall: Number, private val enhet: Tidsenhet){
    internal fun zero() = SpesifikkTid(0, enhet)
    operator fun plus(other: SpesifikkTid): SpesifikkTid? {
        return oversettTilSpesifikkTid(oversettTilBaseEnhet(this) + oversettTilBaseEnhet(other), this.enhet)
    }

    private fun oversettTilSpesifikkTid(any: Any, enhet: Tidsenhet): SpesifikkTid {
        TODO("Not yet implemented")
    }

    private fun oversettTilBaseEnhet(spesifikkTid: SpesifikkTid): Double {
        TODO("Not yet implemented")
    }


    /* override operator fun plus(other: Tidsenhet)
             = SpesifikkTid(this.antall + konvertertAntallEllerZero(other), this.enhet)

     override operator fun minus(other: RatioQuantity) = this + -other

     private fun konvertertAntallEllerZero(other:Tidsenhet){
         if(other is SpesifikkTid) convertedAmount(other) else 0.0
     }*/
}

val Number.arbeidsdager get() = SpesifikkTid(this, Tidsenhet.arbeidsdag)
val Number.arbeidsuker get() = SpesifikkTid(this, Tidsenhet.arbeidsuke)