package no.nav.dagpenger.behandling.regel.dato

import no.nav.dagpenger.behandling.LesbarOpplysninger
import no.nav.dagpenger.behandling.Opplysningstype
import no.nav.dagpenger.behandling.Regelsett
import no.nav.dagpenger.behandling.regel.Regel
import java.time.LocalDate

internal class FørEllerLik(
    produserer: Opplysningstype<Boolean>,
    private val dato: Opplysningstype<LocalDate>,
    private val tom: Opplysningstype<LocalDate>,
) : Regel<Boolean>(produserer, listOf(dato, tom)) {
    override fun kjør(opplysninger: LesbarOpplysninger): Boolean {
        val a = opplysninger.finnOpplysning(dato).verdi
        val b = opplysninger.finnOpplysning(tom).verdi
        return a.isBefore(b) || a.isEqual(b)
    }

    override fun toString() = "$dato er før eller lik $tom"
}

fun Regelsett.førEllerLik(
    gjelderFra: LocalDate,
    produserer: Opplysningstype<Boolean>,
    dato: Opplysningstype<LocalDate>,
    tom: Opplysningstype<LocalDate>,
): Regel<Boolean> {
    return FørEllerLik(produserer, dato, tom).also { leggTil(gjelderFra, produserer, it) }
}

fun Regelsett.førEllerLik(
    produserer: Opplysningstype<Boolean>,
    dato: Opplysningstype<LocalDate>,
    tom: Opplysningstype<LocalDate>,
) = førEllerLik(LocalDate.MIN, produserer, dato, tom)
