package no.nav.dagpenger.opplysning.regel

import no.nav.dagpenger.opplysning.LesbarOpplysninger
import no.nav.dagpenger.opplysning.Opplysningstype

class Substraksjon internal constructor(
    produserer: Opplysningstype<Double>,
    private vararg val opplysningstyper: Opplysningstype<Double>,
) : Regel<Double>(produserer, opplysningstyper.toList()) {
    override fun kjør(opplysninger: LesbarOpplysninger): Double {
        val verdier = opplysninger.finnAlle(opplysningstyper.toList()).map { it.verdi as Double }
        return verdier.reduce { acc, d -> acc - d }
    }

    override fun toString() = "Substraksjon av ${opplysningstyper.joinToString(", ")}"
}

fun Opplysningstype<Double>.substraksjon(vararg opplysningstype: Opplysningstype<Double>) = Substraksjon(this, *opplysningstype)

operator fun Opplysningstype<Double>.minus(opplysningstype: Opplysningstype<Double>) = Substraksjon(opplysningstype, opplysningstype)
