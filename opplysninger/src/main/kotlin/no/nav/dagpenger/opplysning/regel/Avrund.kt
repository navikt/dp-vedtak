package no.nav.dagpenger.opplysning.regel

import no.nav.dagpenger.opplysning.LesbarOpplysninger
import no.nav.dagpenger.opplysning.Opplysningstype
import no.nav.dagpenger.opplysning.verdier.Beløp

class Avrund(
    produserer: Opplysningstype<Int>,
    private val beløp: Opplysningstype<Beløp>,
) : Regel<Int>(produserer, listOf(beløp)) {
    override fun kjør(opplysninger: LesbarOpplysninger): Int {
        val verdi = opplysninger.finnOpplysning(beløp).verdi
        return verdi.avrundet.intValueExact()
    }
}

fun Opplysningstype<Int>.avrund(grunnlag: Opplysningstype<Beløp>) = Avrund(this, grunnlag)
