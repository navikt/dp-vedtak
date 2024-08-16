package no.nav.dagpenger.opplysning.regel

import no.nav.dagpenger.opplysning.LesbarOpplysninger
import no.nav.dagpenger.opplysning.Opplysningstype
import no.nav.dagpenger.opplysning.verdier.Beløp

class Multiplikasjon<T : Comparable<T>> internal constructor(
    produserer: Opplysningstype<Beløp>,
    private val beløp: Opplysningstype<Beløp>,
    private val faktor: Opplysningstype<T>,
    private val operasjon: (Beløp, T) -> Beløp,
) : Regel<Beløp>(produserer, listOf(beløp, faktor)) {
    override fun kjør(opplysninger: LesbarOpplysninger): Beløp {
        val a = opplysninger.finnOpplysning(beløp)
        val b = opplysninger.finnOpplysning(faktor)
        return operasjon(a.verdi, b.verdi)
    }

    override fun toString(): String = "Multiplikasjon av $beløp med $faktor"
}

@JvmName("multiplikasjonDouble")
fun Opplysningstype<Beløp>.multiplikasjon(
    beløp: Opplysningstype<Beløp>,
    faktor: Opplysningstype<Double>,
) = Multiplikasjon(
    this,
    beløp,
    faktor,
) { a, b -> a * b }

@JvmName("multiplikasjonInt")
fun Opplysningstype<Beløp>.multiplikasjon(
    beløp: Opplysningstype<Beløp>,
    faktor: Opplysningstype<Int>,
) = Multiplikasjon(
    this,
    beløp,
    faktor,
) { a, b -> a * b }
