package no.nav.dagpenger.behandling.regel.dato

import no.nav.dagpenger.behandling.LesbarOpplysninger
import no.nav.dagpenger.behandling.Opplysningstype
import no.nav.dagpenger.behandling.regel.Regel
import java.time.LocalDate

class LeggTilÅr internal constructor(
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

fun Opplysningstype<LocalDate>.leggTilÅr(
    dato: Opplysningstype<LocalDate>,
    antallÅr: Opplysningstype<Int>,
) = LeggTilÅr(this, dato, antallÅr)
