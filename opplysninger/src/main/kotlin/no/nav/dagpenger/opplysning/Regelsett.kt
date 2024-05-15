package no.nav.dagpenger.opplysning

import no.nav.dagpenger.opplysning.regel.Regel
import java.time.LocalDate

class Regelsett(val navn: String, val startverdier: Startverdier = { emptyList() }, block: Regelsett.() -> Unit = {}) {
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
}

typealias Startverdier = () -> List<Opplysning<*>>
