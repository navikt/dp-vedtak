package no.nav.dagpenger.opplysning.verdier

class Stønadsperiode(
    private val antall: Int,
) : Comparable<Stønadsperiode> {
    companion object {
        val arbeidsdager = 5

        fun fraUker(uker: Int) = Stønadsperiode(uker * arbeidsdager)
    }

    val uker get() = antall / arbeidsdager
    val dager get() = antall

    override fun compareTo(other: Stønadsperiode): Int = antall.compareTo(other.antall)

    override fun equals(other: Any?): Boolean = other is Stønadsperiode && other.antall == antall

    operator fun minus(forbruk: Stønadsperiode): Stønadsperiode {
        require(forbruk.antall <= antall) { "Kan ikke trekke flere dager enn det som er igjen. Er $antall igjen" }
        return Stønadsperiode(antall - forbruk.antall)
    }

    operator fun plus(korrigering: Stønadsperiode): Stønadsperiode = Stønadsperiode(antall + korrigering.antall)

    override fun toString() = "Stønadsdager(antall=$antall)"
}
