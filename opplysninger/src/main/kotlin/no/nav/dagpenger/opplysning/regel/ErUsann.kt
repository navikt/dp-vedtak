package no.nav.dagpenger.opplysning.regel

import no.nav.dagpenger.opplysning.LesbarOpplysninger
import no.nav.dagpenger.opplysning.Opplysningstype

class ErUsann internal constructor(
    produserer: Opplysningstype<Boolean>,
    private val opplysningstype: Opplysningstype<Boolean>,
) : Regel<Boolean>(produserer, listOf(opplysningstype)) {
    override fun kjør(opplysninger: LesbarOpplysninger): Boolean = !opplysninger.finnOpplysning(opplysningstype).verdi

    override fun toString() = "Opplysning $opplysningstype er usann"
}

fun Opplysningstype<Boolean>.erUsann(opplysningstype: Opplysningstype<Boolean>) = ErUsann(this, opplysningstype)

fun Opplysningstype<Boolean>.ikkeOppfylt(opplysningstype: Opplysningstype<Boolean>) = ErUsann(this, opplysningstype)
