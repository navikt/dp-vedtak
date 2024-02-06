package no.nav.dagpenger.behandling.regel

import no.nav.dagpenger.behandling.LesbarOpplysninger
import no.nav.dagpenger.behandling.Opplysningstype
import no.nav.dagpenger.behandling.Regelsett
import java.time.LocalDate

internal class LeggTilÅr(
    produserer: Opplysningstype<LocalDate>,
    private val dato: Opplysningstype<LocalDate>,
    private val antallÅr: Opplysningstype<Int>,
) : Regel<LocalDate>(produserer, listOf(dato, antallÅr)) {
    override fun kjør(opplysninger: LesbarOpplysninger): LocalDate {
        val a = opplysninger.finnOpplysning(dato).verdi
        return a.plusYears(opplysninger.finnOpplysning(antallÅr).verdi.toLong())
    }

    override fun toString() = "Legg til $antallÅr på $dato"
}

fun Regelsett.leggTilÅr(
    gjelderFra: LocalDate,
    produserer: Opplysningstype<LocalDate>,
    dato: Opplysningstype<LocalDate>,
    antallÅr: Opplysningstype<Int>,
): Regel<LocalDate> = LeggTilÅr(produserer, dato, antallÅr).also { leggTil(gjelderFra, produserer, it) }

fun Regelsett.leggTilÅr(
    produserer: Opplysningstype<LocalDate>,
    dato: Opplysningstype<LocalDate>,
    antallÅr: Opplysningstype<Int>,
) = leggTilÅr(LocalDate.MIN, produserer, dato, antallÅr)
