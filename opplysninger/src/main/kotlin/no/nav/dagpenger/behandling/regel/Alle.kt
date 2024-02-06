package no.nav.dagpenger.behandling.regel

import no.nav.dagpenger.behandling.LesbarOpplysninger
import no.nav.dagpenger.behandling.Opplysningstype
import no.nav.dagpenger.behandling.Regelsett
import java.time.LocalDate

internal class Alle(
    produserer: Opplysningstype<Boolean>,
    private vararg val opplysningstyper: Opplysningstype<Boolean>,
) : Regel<Boolean>(produserer, opplysningstyper.toList()) {
    override fun kjør(opplysninger: LesbarOpplysninger) = opplysninger.finnAlle(opplysningstyper.toList()).all { it.verdi as Boolean }

    override fun toString() = "Alle ${opplysningstyper.joinToString(", ")} er sanne"
}

fun Regelsett.alle(
    gjelderFra: LocalDate,
    produserer: Opplysningstype<Boolean>,
    vararg opplysningstype: Opplysningstype<Boolean>,
): Regel<Boolean> {
    return Alle(produserer, *opplysningstype).also { leggTil(gjelderFra, produserer, it) }
}

fun Regelsett.alle(
    produserer: Opplysningstype<Boolean>,
    vararg opplysningstype: Opplysningstype<Boolean>,
) = alle(LocalDate.MIN, produserer, *opplysningstype)
