package no.nav.dagpenger.vedtak.modell.entitet

class Stønadsdager(private val dager: Int) : Comparable<Stønadsdager> {

    fun stønadsdager() = this.dager

    override fun compareTo(other: Stønadsdager) = this.dager.compareTo(other.dager)

    override fun equals(other: Any?) = other is Stønadsdager && this.dager == other.dager

    override fun hashCode() = dager.hashCode()

    override fun toString() = "Stønadsdager($dager)"

    infix operator fun plus(other: Stønadsdager) = Stønadsdager(dager = this.dager + other.dager)

    infix operator fun minus(other: Stønadsdager) = Stønadsdager(dager = this.dager - other.dager)
}
