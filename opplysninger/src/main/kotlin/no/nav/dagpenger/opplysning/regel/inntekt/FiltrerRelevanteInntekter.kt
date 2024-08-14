package no.nav.dagpenger.opplysning.regel.inntekt

import no.nav.dagpenger.inntekt.v1.InntektKlasse
import no.nav.dagpenger.opplysning.LesbarOpplysninger
import no.nav.dagpenger.opplysning.Opplysningstype
import no.nav.dagpenger.opplysning.regel.Regel
import no.nav.dagpenger.opplysning.verdier.Inntekt
import java.util.EnumSet

// TODO : Flytt denne til dagpenger-regelverks modul
class FiltrerRelevanteInntekter(
    produserer: Opplysningstype<Inntekt>,
    private val ufiltrertInntekt: Opplysningstype<Inntekt>,
) : Regel<Inntekt>(produserer, listOf(ufiltrertInntekt)) {
    private val inntektklasser =
        EnumSet
            .of(
                InntektKlasse.ARBEIDSINNTEKT,
                InntektKlasse.DAGPENGER,
                InntektKlasse.SYKEPENGER,
                InntektKlasse.TILTAKSLØNN,
                InntektKlasse.PLEIEPENGER,
                InntektKlasse.OPPLÆRINGSPENGER,
                InntektKlasse.OMSORGSPENGER,
            ).toList()

    override fun kjør(opplysninger: LesbarOpplysninger): Inntekt {
        val inntekt = opplysninger.finnOpplysning(this.ufiltrertInntekt).verdi
        val relevanteInntekter =
            inntekt.verdi.inntektsListe.filter {
                it.klassifiserteInntekter.any { inntektMåned ->
                    inntektMåned.inntektKlasse in
                        inntektklasser
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

fun Opplysningstype<Inntekt>.filtrerRelevanteInntekter(ufiltrertInntekt: Opplysningstype<Inntekt>) =
    FiltrerRelevanteInntekter(this, ufiltrertInntekt)
