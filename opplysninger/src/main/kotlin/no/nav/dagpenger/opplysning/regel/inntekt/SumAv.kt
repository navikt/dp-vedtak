package no.nav.dagpenger.opplysning.regel.inntekt

import no.nav.dagpenger.opplysning.LesbarOpplysninger
import no.nav.dagpenger.opplysning.Opplysningstype
import no.nav.dagpenger.opplysning.regel.Regel
import no.nav.dagpenger.opplysning.verdier.Beløp
import no.nav.dagpenger.opplysning.verdier.Inntekt

class SumAv internal constructor(
    produserer: Opplysningstype<Beløp>,
    private val inntekt: Opplysningstype<Inntekt>,
) : Regel<Beløp>(produserer, listOf(inntekt)) {
    override fun kjør(opplysninger: LesbarOpplysninger): Beløp {
        val verdier = opplysninger.finnOpplysning(inntekt).verdi

        return Beløp(verdier.sum())
    }

    override fun toString() = "Summerer all inntekt"
}

fun Opplysningstype<Beløp>.sumAv(inntekt: Opplysningstype<Inntekt>) = SumAv(this, inntekt)
