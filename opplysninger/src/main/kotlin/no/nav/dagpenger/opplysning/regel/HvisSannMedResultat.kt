package no.nav.dagpenger.opplysning.regel

import no.nav.dagpenger.opplysning.LesbarOpplysninger
import no.nav.dagpenger.opplysning.Opplysningstype
import no.nav.dagpenger.opplysning.verdier.Stønadsperiode

class HvisSannMedResultat(
    produserer: Opplysningstype<Stønadsperiode>,
    private val sjekk: Opplysningstype<Boolean>,
    private val hvisSann: Opplysningstype<Stønadsperiode>,
    private val hvisUsann: Opplysningstype<Stønadsperiode>,
) : Regel<Stønadsperiode>(produserer, listOf(sjekk, hvisSann, hvisUsann)) {
    override fun kjør(opplysninger: LesbarOpplysninger): Stønadsperiode {
        val sjekk = opplysninger.finnOpplysning(sjekk).verdi
        val hvisSann = opplysninger.finnOpplysning(hvisSann).verdi
        val hvisUsann = opplysninger.finnOpplysning(hvisUsann).verdi

        return if (sjekk) hvisSann else hvisUsann
    }

    override fun toString() = "Hvis $sjekk er sann, returner $hvisSann, ellers returner $hvisUsann"
}

fun Opplysningstype<Stønadsperiode>.hvisSannMedResultat(
    sjekk: Opplysningstype<Boolean>,
    hvisSann: Opplysningstype<Stønadsperiode>,
    hvisUsann: Opplysningstype<Stønadsperiode>,
) = HvisSannMedResultat(this, sjekk, hvisSann, hvisUsann)
