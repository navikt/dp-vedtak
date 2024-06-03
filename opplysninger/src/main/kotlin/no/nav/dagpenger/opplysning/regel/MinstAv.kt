package no.nav.dagpenger.opplysning.regel

import no.nav.dagpenger.opplysning.LesbarOpplysninger
import no.nav.dagpenger.opplysning.Opplysningstype

class MinstAv internal constructor(
    produserer: Opplysningstype<Double>,
    private vararg val opplysningstyper: Opplysningstype<*>,
) : Regel<Double>(produserer, opplysningstyper.toList()) {
    override fun kjør(opplysninger: LesbarOpplysninger): Double {
        val verdier = opplysninger.finnAlle(opplysningstyper.toList()).map { it.verdi as Double }

        return verdier.min()
    }

    override fun toString() = "Velger den laveste verdi av ${opplysningstyper.toList()}"
}

fun Opplysningstype<Double>.minstAv(vararg verdi: Opplysningstype<Double>) = MinstAv(this, *verdi)
