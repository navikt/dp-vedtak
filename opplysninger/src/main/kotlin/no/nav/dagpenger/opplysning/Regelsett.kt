package no.nav.dagpenger.opplysning

import no.nav.dagpenger.opplysning.regel.Ekstern
import no.nav.dagpenger.opplysning.regel.Regel
import java.time.LocalDate

class Regelsett(
    val navn: String,
    block: Regelsett.() -> Unit = {},
) {
    private val regler: MutableMap<Opplysningstype<*>, TemporalCollection<Regel<*>>> = mutableMapOf()

    init {
        block()
    }

    fun regler(forDato: LocalDate = LocalDate.MIN) = regler.map { it.value.get(forDato) }.toList()

    private fun leggTil(
        gjelderFra: LocalDate,
        regel: Regel<*>,
    ) = regler.computeIfAbsent(regel.produserer) { TemporalCollection() }.put(gjelderFra, regel)

    fun <T : Comparable<T>> regel(
        produserer: Opplysningstype<T>,
        gjelderFraOgMed: LocalDate = LocalDate.MIN,
        block: Opplysningstype<T>.() -> Regel<*>,
    ) = leggTil(gjelderFraOgMed, produserer.block())

    val produserer: Set<Opplysningstype<*>>
        by lazy { regler.map { it.key }.toSet() }

    fun produserer(opplysningstype: Opplysningstype<*>) = produserer.contains(opplysningstype)

    val avhengerAv: Set<Opplysningstype<*>>
        by lazy {
            regler.flatMap { it.value.getAll().flatMap { regel -> regel.avhengerAv } }.toSet().minus(produserer)
        }

    fun avhengerAv(opplysningstype: Opplysningstype<*>) = avhengerAv.contains(opplysningstype)

    val behov by lazy {
        regler
            .flatMap { it.value.getAll() }
            .toSet()
            .filterIsInstance<Ekstern<*>>()
            .map { it.produserer }
    }

    override fun toString() = "Regelsett(navn=$navn)"
}
