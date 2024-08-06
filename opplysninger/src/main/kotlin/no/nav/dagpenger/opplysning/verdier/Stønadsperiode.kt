package no.nav.dagpenger.opplysning.verdier

data class Stønadsperiode(
    private val antall: Int,
    private val arbeidsdager: Int = 5,
) : Comparable<Stønadsperiode> {
    val uker get() = antall

    fun tilStønadsdager(): Stønadsdager = Stønadsdager(uker * arbeidsdager)

    override fun compareTo(other: Stønadsperiode): Int = uker.compareTo(other.uker)
}
