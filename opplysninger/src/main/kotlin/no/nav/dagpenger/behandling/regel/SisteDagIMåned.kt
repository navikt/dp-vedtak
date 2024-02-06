package no.nav.dagpenger.behandling.regel

import no.nav.dagpenger.behandling.LesbarOpplysninger
import no.nav.dagpenger.behandling.Opplysningstype
import no.nav.dagpenger.behandling.Regelsett
import java.time.LocalDate

internal class SisteDagIMåned(
    produserer: Opplysningstype<LocalDate>,
    private val dato: Opplysningstype<LocalDate>,
) : Regel<LocalDate>(produserer, listOf(dato)) {
    override fun kjør(opplysninger: LesbarOpplysninger): LocalDate {
        val a = opplysninger.finnOpplysning(dato).verdi
        return a.withDayOfMonth(a.lengthOfMonth())
    }

    override fun toString() = "Siste dag i måneden for $dato"
}

fun Regelsett.sisteDagIMåned(
    gjelderFra: LocalDate,
    produserer: Opplysningstype<LocalDate>,
    dato: Opplysningstype<LocalDate>,
): Regel<LocalDate> = SisteDagIMåned(produserer, dato).also { leggTil(gjelderFra, produserer, it) }

fun Regelsett.sisteDagIMåned(
    produserer: Opplysningstype<LocalDate>,
    dato: Opplysningstype<LocalDate>,
) = sisteDagIMåned(LocalDate.MIN, produserer, dato)
