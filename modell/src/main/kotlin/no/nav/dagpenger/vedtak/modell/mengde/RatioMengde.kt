package no.nav.dagpenger.vedtak.modell.mengde

// Forstår ulike mengder hvor det finnes et absolutt nullpunkt, som f.eks. alder, penger, tid
open class RatioMengde internal constructor(private val mengde: Number, enhet: Enhet) :
    IntervallMengde(mengde, enhet),
    Comparable<RatioMengde> {
    operator fun unaryPlus() = this

    operator fun unaryMinus() = RatioMengde(-amount, enhet)

    operator fun plus(other: RatioMengde) = RatioMengde(this.amount + convertedAmount(other), enhet)

    operator fun minus(other: RatioMengde) = this + -other

    override fun compareTo(other: RatioMengde): Int {
        require(this.enhet.isCompatible(other.enhet)) { "Kan bare sammenligne med samme enhet." }
        return when {
            this.mengde.toDouble() == other.mengde.toDouble() -> 0
            this.mengde.toDouble() < other.mengde.toDouble() -> -1
            else -> 1
        }
    }
}
