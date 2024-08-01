package no.nav.dagpenger.opplysning.regel

import no.nav.dagpenger.opplysning.LesbarOpplysninger
import no.nav.dagpenger.opplysning.Opplysningstype

class HøyesteAv(
    produserer: Opplysningstype<Int>,
    vararg val opplysningstyper: Opplysningstype<Int>,
) : Regel<Int>(produserer, opplysningstyper.toList()) {
    override fun kjør(opplysninger: LesbarOpplysninger): Int =
        opplysningstyper.maxOfOrNull { opplysningstype -> opplysninger.finnOpplysning(opplysningstype).verdi } ?: 0
}

fun Opplysningstype<Int>.høyesteAv(vararg opplysningstype: Opplysningstype<Int>) = HøyesteAv(this, *opplysningstype)
