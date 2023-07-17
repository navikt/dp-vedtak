package no.nav.dagpenger.vedtak.modell.entitet

internal class Prosent(prosent: Number) : Comparable<Prosent> {
    private val prosent = prosent.toDouble()
    private val prosentfaktor get(): Double = prosent / 100

    init {
        require(this.prosent >= 0) { "Arbeidsprosent må være større enn eller lik 0, er ${this.prosent}" }
    }

    companion object {
        fun Collection<Prosent>.summer() = Prosent(this.sumOf { it.prosent })
    }

    infix operator fun div(nevner: Double) = Prosent(this.prosent / nevner)

    infix fun av(vanligArbeidstid: Timer) = vanligArbeidstid * prosentfaktor

    infix operator fun times(beløp: Beløp) = beløp * prosentfaktor

    override fun compareTo(other: Prosent): Int = this.prosent.compareTo(other.prosent)

    override fun equals(other: Any?): Boolean = other is Prosent && other.prosent == this.prosent

    override fun hashCode() = prosent.hashCode()

    override fun toString() = "Prosent($prosent)"
}
