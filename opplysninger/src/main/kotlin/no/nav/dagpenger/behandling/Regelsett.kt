package no.nav.dagpenger.behandling

import no.nav.dagpenger.behandling.regel.Regel
import java.time.LocalDate

class Regelsett(val navn: String, block: Regelsett.() -> Unit = {}) {
    private val regler: MutableMap<Opplysningstype<*>, TemporalCollection<Regel<*>>> = mutableMapOf()

    init {
        block()
    }

    fun regler(forDato: LocalDate = LocalDate.MIN) = regler.map { it.value.get(forDato) }.toList()

    fun leggTil(
        gjelderFra: LocalDate,
        regel: Regel<*>,
    ) = regler.computeIfAbsent(regel.produserer) { TemporalCollection() }.put(gjelderFra, regel)

    fun leggTil(regel: Regel<*>) = leggTil(LocalDate.MIN, regel)

    fun regel(
        gjelderFra: LocalDate = LocalDate.MIN,
        block: () -> Regel<*>,
    ) = leggTil(gjelderFra, block())
}
