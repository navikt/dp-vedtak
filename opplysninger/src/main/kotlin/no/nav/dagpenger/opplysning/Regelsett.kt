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
        // Sjekk om reglene skal vurderes
        if (opplysninger != null && !skalVurderes(opplysninger)) return emptyList()
        val egneRegler = regler.values.map { it.get(forDato) }

        // Finn regler fra barn rekursivt
        val barnRegler = regelsett.flatMap { it.regler(forDato, opplysninger) }

        // Slå sammen egne og barnas regler
        return egneRegler + barnRegler
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
