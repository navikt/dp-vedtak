package no.nav.dagpenger.behandling.regel

import no.nav.dagpenger.behandling.LesbarOpplysninger
import no.nav.dagpenger.behandling.Opplysningstype
import no.nav.dagpenger.behandling.Regelsett
import java.time.LocalDate

internal class StørreEnn(
    produserer: Opplysningstype<Boolean>,
    private val a: Opplysningstype<Double>,
    private val b: Opplysningstype<Double>,
) : Regel<Boolean>(produserer, listOf(a, b)) {
    override fun kjør(opplysninger: LesbarOpplysninger): Boolean {
        val a = opplysninger.finnOpplysning(a).verdi
        val b = opplysninger.finnOpplysning(b).verdi
        return a > b
    }

    override fun toString(): String {
        return "Større enn $a > $b"
    }
}

fun Regelsett.størreEnn(
    gjelderFra: LocalDate,
    produserer: Opplysningstype<Boolean>,
    er: Opplysningstype<Double>,
    størreEnn: Opplysningstype<Double>,
): Regel<Boolean> {
    return StørreEnn(produserer, er, størreEnn).also { leggTil(gjelderFra, produserer, it) }
}

fun Regelsett.størreEnn(
    produserer: Opplysningstype<Boolean>,
    er: Opplysningstype<Double>,
    størreEnn: Opplysningstype<Double>,
) = størreEnn(LocalDate.MIN, produserer, er, størreEnn)
