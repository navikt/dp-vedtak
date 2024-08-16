package no.nav.dagpenger.opplysning.regel.inntekt

import no.nav.dagpenger.inntekt.v1.InntektKlasse
import no.nav.dagpenger.opplysning.LesbarOpplysninger
import no.nav.dagpenger.opplysning.Opplysningstype
import no.nav.dagpenger.opplysning.regel.Regel
import no.nav.dagpenger.opplysning.verdier.Inntekt

// TODO : Flytt denne til dagpenger-regelverks modul
class FiltrerRelevanteInntekter(
    produserer: Opplysningstype<Inntekt>,
    private val ufiltrertInntekt: Opplysningstype<Inntekt>,
    private val inntektsklasser: List<InntektKlasse>,
) : Regel<Inntekt>(produserer, listOf(ufiltrertInntekt)) {
    override fun kjør(opplysninger: LesbarOpplysninger): Inntekt {
        val inntekt = opplysninger.finnOpplysning(this.ufiltrertInntekt).verdi
        val relevanteInntekter =
            inntekt.verdi.inntektsListe.filter {
                it.klassifiserteInntekter.any { inntektMåned ->
                    inntektMåned.inntektKlasse in
                        inntektsklasser
                }
            }
        return Inntekt(
            no.nav.dagpenger.inntekt.v1.Inntekt(
                inntektsId = inntekt.verdi.inntektsId,
                inntektsListe = relevanteInntekter,
                sisteAvsluttendeKalenderMåned = inntekt.verdi.sisteAvsluttendeKalenderMåned,
            ),
        )
    }
}

fun Opplysningstype<Inntekt>.filtrerRelevanteInntekter(
    ufiltrertInntekt: Opplysningstype<Inntekt>,
    inntektklasser: List<InntektKlasse>,
) = FiltrerRelevanteInntekter(
    this,
    ufiltrertInntekt,
    inntektklasser,
)
