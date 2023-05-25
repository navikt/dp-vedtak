package no.nav.dagpenger.vedtak.modell.entitet

class Stønadsperiode(private val stønadsdager: Int) {
    constructor(dagpengeperiode: Dagpengeperiode) : this(dagpengeperiode * 5)

    fun stønadsdager() = this.stønadsdager

    override fun equals(other: Any?) = other is Stønadsperiode && this.stønadsdager == other.stønadsdager

    override fun hashCode() = stønadsdager.hashCode()
}
