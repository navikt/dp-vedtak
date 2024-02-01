package no.nav.dagpenger.behandling

import no.nav.dagpenger.behandling.regel.Regel
import java.time.LocalDate

class Regelsett {
    private val regler: MutableMap<Opplysningstype<*>, TemporalCollection<Regel<*>>> = mutableMapOf()

    fun regler(forDato: LocalDate = LocalDate.MIN) =
        regler
            .map { it.value.get(forDato) }
            .toList()

    fun leggTil(
        gjelderFra: LocalDate,
        opplysningstype: Opplysningstype<*>,
        regel: Regel<*>,
    ) {
        regler.computeIfAbsent(opplysningstype) { TemporalCollection() }.put(gjelderFra, regel)
    }
}
