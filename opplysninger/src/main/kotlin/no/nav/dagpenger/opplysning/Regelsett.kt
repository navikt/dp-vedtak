package no.nav.dagpenger.opplysning

import no.nav.dagpenger.opplysning.regel.Regel
import java.time.LocalDate

class Regelsett(val navn: String, block: Regelsett.() -> Unit = {}) {
    private val regler: MutableMap<Opplysningstype<*>, TemporalCollection<Regel<*>>> = mutableMapOf()
    private var kriterie: Kriterie = { true }
    private val regelsett: MutableList<Regelsett> = mutableListOf()

    init {
        block()
    }

    fun regler(
        forDato: LocalDate = LocalDate.MIN,
        opplysninger: Opplysninger? = null,
    ): List<Regel<*>> {
        // TODO: Rydd opp i rekursiviteten her
        if (opplysninger != null && !skalVurderes(opplysninger)) return emptyList()
        return regler.map { it.value.get(forDato) }.toList() +
            regelsett.filter { opplysninger == null || it.skalVurderes(opplysninger) }
                .flatMap { it.regler(forDato) }
    }

    private fun leggTil(
        gjelderFra: LocalDate,
        regel: Regel<*>,
    ) = regler.computeIfAbsent(regel.produserer) { TemporalCollection() }.put(gjelderFra, regel)

    fun <T : Comparable<T>> regel(
        produserer: Opplysningstype<T>,
        gjelderFraOgMed: LocalDate = LocalDate.MIN,
        block: Opplysningstype<T>.() -> Regel<*>,
    ) = leggTil(gjelderFraOgMed, produserer.block())

    fun skalVurderes(block: Kriterie) {
        kriterie = block
    }

    fun skalVurderes(opplysninger: Opplysninger): Boolean = kriterie(opplysninger)

    fun regelsett(
        navn: String,
        block: Regelsett.() -> Unit,
    ) {
        regelsett.add(Regelsett(navn, block))
    }
}

typealias Kriterie = Opplysninger.() -> Boolean
