package no.nav.dagpenger.opplysning.regel.inntekt

import no.nav.dagpenger.opplysning.LesbarOpplysninger
import no.nav.dagpenger.opplysning.Opplysningstype
import no.nav.dagpenger.opplysning.regel.Regel
import no.nav.dagpenger.opplysning.verdier.Beløp
import no.nav.dagpenger.opplysning.verdier.Inntekt

class SummerFørstePeriode(
    produserer: Opplysningstype<Beløp>,
    private val inntekt: Opplysningstype<Inntekt>,
) : Regel<Beløp>(produserer, listOf(inntekt)) {
    override fun kjør(opplysninger: LesbarOpplysninger): Beløp {
        val verdi = opplysninger.finnOpplysning(inntekt).verdi
        val sum =
            verdi.verdi.splitIntoInntektsPerioder().first.sumOf { klassifisertInntektMåned ->
                klassifisertInntektMåned.klassifiserteInntekter.sumOf { it.beløp }
            }

        return Beløp(sum)
    }

    override fun toString() = "Summerer første periode (12 måneder) av inntekt $inntekt"
}

fun Opplysningstype<Beløp>.summerFørstePeriode(inntekt: Opplysningstype<Inntekt>) = SummerFørstePeriode(this, inntekt)
