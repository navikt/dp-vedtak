package no.nav.dagpenger.opplysning.regel

import no.nav.dagpenger.opplysning.LesbarOpplysninger
import no.nav.dagpenger.opplysning.Opplysningstype

class HvisSannMedResultat(
    produserer: Opplysningstype<Int>,
    private val sjekk: Opplysningstype<Boolean>,
    private val hvisSann: Opplysningstype<Int>,
    private val hvisUsann: Opplysningstype<Int>,
) : Regel<Int>(produserer, listOf(sjekk, hvisSann, hvisUsann)) {
    override fun kjør(opplysninger: LesbarOpplysninger): Int {
        val sjekk = opplysninger.finnOpplysning(sjekk).verdi
        val hvisSann = opplysninger.finnOpplysning(hvisSann).verdi
        val hvisUsann = opplysninger.finnOpplysning(hvisUsann).verdi

        return if (sjekk) hvisSann else hvisUsann
    }

    override fun toString() = "Hvis $sjekk er sann, returner $hvisSann, ellers returner $hvisUsann"
}

fun Opplysningstype<Int>.hvisSannMedResultat(
    sjekk: Opplysningstype<Boolean>,
    hvisSann: Opplysningstype<Int>,
    hvisUsann: Opplysningstype<Int>,
) = HvisSannMedResultat(this, sjekk, hvisSann, hvisUsann)
