package no.nav.dagpenger.behandling.regel

import no.nav.dagpenger.behandling.LesbarOpplysninger
import no.nav.dagpenger.behandling.Opplysningstype

class EnAv internal constructor(
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

fun Opplysningstype<Boolean>.enAv(vararg opplysningstype: Opplysningstype<Boolean>) = EnAv(this, *opplysningstype)
