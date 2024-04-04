package no.nav.dagpenger.opplysning.regel

import no.nav.dagpenger.opplysning.LesbarOpplysninger
import no.nav.dagpenger.opplysning.Opplysningstype

class ErSann internal constructor(
    produserer: Opplysningstype<Boolean>,
    private val opplysningstype: Opplysningstype<Boolean>,
) : Regel<Boolean>(produserer, listOf(opplysningstype)) {
    override fun kjør(opplysninger: LesbarOpplysninger): Boolean {
        return opplysninger.finnOpplysning(opplysningstype).verdi
    }

    override fun toString() = "Opplysning $opplysningstype er sann"
}

fun Opplysningstype<Boolean>.erSann(opplysningstype: Opplysningstype<Boolean>) = ErSann(this, opplysningstype)
