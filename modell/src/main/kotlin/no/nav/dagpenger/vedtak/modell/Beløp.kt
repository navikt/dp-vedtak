package no.nav.dagpenger.vedtak.modell

class Beløp private constructor(val verdi: Number) {
    companion object {
        val Number.beløp get() = Beløp(this)
    }

    override fun equals(other: Any?) = other is Beløp && other.verdi == this.verdi
    override fun hashCode() = verdi.hashCode()
}