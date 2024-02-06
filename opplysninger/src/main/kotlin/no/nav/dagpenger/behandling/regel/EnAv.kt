package no.nav.dagpenger.behandling.regel

import no.nav.dagpenger.behandling.LesbarOpplysninger
import no.nav.dagpenger.behandling.Opplysningstype
import no.nav.dagpenger.behandling.Regelsett
import java.time.LocalDate

internal class EnAv(
    produserer: Opplysningstype<Boolean>,
    private vararg val opplysningstyper: Opplysningstype<Boolean>,
) : Regel<Boolean>(produserer, opplysningstyper.toList()) {
    override fun kjør(opplysninger: LesbarOpplysninger): Boolean {
        return opplysninger.finnAlle(opplysningstyper.toList()).any { it.verdi as Boolean }
    }

    override fun toString(): String {
        return "En av ${opplysningstyper.joinToString(", ")} er sanne"
    }
}

fun Regelsett.enAv(
    gjelderFra: LocalDate,
    produserer: Opplysningstype<Boolean>,
    vararg opplysningstype: Opplysningstype<Boolean>,
): Regel<Boolean> {
    return EnAv(produserer, *opplysningstype).also { leggTil(gjelderFra, produserer, it) }
}

fun Regelsett.enAv(
    produserer: Opplysningstype<Boolean>,
    vararg opplysningstype: Opplysningstype<Boolean>,
) = enAv(LocalDate.MIN, produserer, *opplysningstype)
