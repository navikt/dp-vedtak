package no.nav.dagpenger.opplysning.verdier

class Stønadsdager(
    private val antall: Int,
) : Comparable<Stønadsdager> {
    val dager get() = antall

    override fun compareTo(other: Stønadsdager): Int = antall.compareTo(other.antall)

    override fun equals(other: Any?): Boolean = other is Stønadsdager && other.antall == antall

    operator fun minus(forbruk: Stønadsdager): Stønadsdager {
        require(forbruk.antall <= antall) { "Kan ikke trekke flere dager enn det som er igjen. Er $antall igjen" }
        return Stønadsdager(antall - forbruk.antall)
    }

    operator fun plus(korrigering: Stønadsdager): Stønadsdager = Stønadsdager(antall + korrigering.antall)

    override fun toString() = "Stønadsdager(antall=$antall)"
}
