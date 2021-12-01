package no.nav.dagpenger.vedtak.modell.mengder

typealias Tid = IntervallMengde

// Understands a specific metric
class Enhet {
    companion object {
        private val arbeidsdag = Enhet()
        private val arbeidsuke = Enhet(5, arbeidsdag)
        val Number.arbeidsdager get() = IntervallMengde(this, arbeidsdag)
        val Number.arbeidsuker get() = IntervallMengde(this, arbeidsuke)
        private val prosent = Enhet()
        private val prosentDag = Enhet(20, prosent)
        private val prosentUke = Enhet(200, prosent)
        val Number.dagsprosent get() = IntervallMengde(this, prosentDag)
        val Number.ukeprosent get() = IntervallMengde(this, prosentUke)
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
