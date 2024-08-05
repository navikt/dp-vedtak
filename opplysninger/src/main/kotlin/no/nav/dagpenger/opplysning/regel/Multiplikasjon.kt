package no.nav.dagpenger.opplysning.regel

import no.nav.dagpenger.opplysning.LesbarOpplysninger
import no.nav.dagpenger.opplysning.Opplysningstype
import no.nav.dagpenger.opplysning.verdier.Beløp

class Multiplikasjon internal constructor(
    produserer: Opplysningstype<Beløp>,
    private val beløp: Opplysningstype<Beløp>,
    private val faktor: Opplysningstype<Double>,
) : Regel<Beløp>(produserer, listOf(beløp, faktor)) {
    override fun kjør(opplysninger: LesbarOpplysninger): Beløp {
        val a = opplysninger.finnOpplysning(beløp)
        val b = opplysninger.finnOpplysning(faktor)
        return a.verdi * b.verdi
    }

    override fun toString(): String = "Multiplikasjon av $beløp med $faktor"
}

fun Opplysningstype<Beløp>.multiplikasjon(
    beløp: Opplysningstype<Beløp>,
    faktor: Opplysningstype<Double>,
) = Multiplikasjon(
    this,
    beløp,
    faktor,
)
