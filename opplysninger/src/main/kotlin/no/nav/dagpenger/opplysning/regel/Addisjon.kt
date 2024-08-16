package no.nav.dagpenger.opplysning.regel

import no.nav.dagpenger.opplysning.LesbarOpplysninger
import no.nav.dagpenger.opplysning.Opplysningstype
import no.nav.dagpenger.opplysning.verdier.Beløp

class Addisjon(
    produserer: Opplysningstype<Beløp>,
    private val ledd1: Opplysningstype<Beløp>,
    private val ledd2: Opplysningstype<Beløp>,
) : Regel<Beløp>(produserer, listOf(ledd1, ledd2)) {
    override fun kjør(opplysninger: LesbarOpplysninger): Beløp {
        val verdi1 = opplysninger.finnOpplysning(ledd1).verdi
        val verdi2 = opplysninger.finnOpplysning(ledd2).verdi
        return verdi1 + verdi2
    }

    override fun toString(): String = "Addisjon av $ledd1 og $ledd2"
}

fun Opplysningstype<Beløp>.addisjon(
    ledd1: Opplysningstype<Beløp>,
    ledd2: Opplysningstype<Beløp>,
) = Addisjon(
    this,
    ledd1,
    ledd2,
)
