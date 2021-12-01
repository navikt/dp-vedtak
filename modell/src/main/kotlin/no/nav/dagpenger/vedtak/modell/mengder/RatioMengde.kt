package no.nav.dagpenger.vedtak.modell.mengder

// Forstår ulike mengder hvor det finnes et absolutt nullpunkt, som f.eks. alder, penger, tid
open class RatioMengde internal constructor(mengde: Number, enhet: Enhet) : IntervallMengde(mengde, enhet) {
    operator fun unaryPlus() = this

    operator fun unaryMinus() = RatioMengde(-amount, enhet)

    operator fun plus(other: RatioMengde) = RatioMengde(this.amount + convertedAmount(other), enhet)

    operator fun minus(other: RatioMengde) = this + -other
}
