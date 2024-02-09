package no.nav.dagpenger.behandling.regel

import no.nav.dagpenger.behandling.LesbarOpplysninger
import no.nav.dagpenger.behandling.Opplysningstype

class Alle internal constructor(
    produserer: Opplysningstype<Boolean>,
    private vararg val opplysningstyper: Opplysningstype<Boolean>,
) : Regel<Boolean>(produserer, opplysningstyper.toList()) {
    override fun kjør(opplysninger: LesbarOpplysninger) = opplysninger.finnAlle(opplysningstyper.toList()).all { it.verdi as Boolean }

    override fun toString() = "Alle ${opplysningstyper.joinToString(", ")} er sanne"
}

fun Opplysningstype<Boolean>.alle(vararg opplysningstype: Opplysningstype<Boolean>) = Alle(this, *opplysningstype)
