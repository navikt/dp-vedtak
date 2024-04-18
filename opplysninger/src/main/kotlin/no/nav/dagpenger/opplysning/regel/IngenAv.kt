package no.nav.dagpenger.opplysning.regel

import no.nav.dagpenger.opplysning.LesbarOpplysninger
import no.nav.dagpenger.opplysning.Opplysningstype

class IngenAv internal constructor(
    produserer: Opplysningstype<Boolean>,
    private vararg val opplysningstyper: Opplysningstype<Boolean>,
) : Regel<Boolean>(produserer, opplysningstyper.toList()) {
    override fun kjør(opplysninger: LesbarOpplysninger): Boolean {
        return opplysninger.finnAlle(opplysningstyper.toList()).none { it.verdi as Boolean }
    }

    override fun toString() = "Ingen av ${opplysningstyper.joinToString(", ")} er sanne"
}

fun Opplysningstype<Boolean>.ingenAv(vararg opplysningstype: Opplysningstype<Boolean>) = IngenAv(this, *opplysningstype)
