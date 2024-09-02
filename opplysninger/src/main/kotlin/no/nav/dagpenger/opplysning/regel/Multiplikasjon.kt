package no.nav.dagpenger.opplysning.regel

import no.nav.dagpenger.opplysning.LesbarOpplysninger
import no.nav.dagpenger.opplysning.Opplysningstype
import no.nav.dagpenger.opplysning.verdier.Beløp

class Multiplikasjon<R : Comparable<R>, Faktor1 : Comparable<Faktor1>, Faktor2 : Comparable<Faktor2>> internal constructor(
    produserer: Opplysningstype<R>,
    private val faktor1: Opplysningstype<Faktor1>,
    private val faktor2: Opplysningstype<Faktor2>,
    private val operasjon: (Faktor1, Faktor2) -> R,
) : Regel<R>(produserer, listOf(faktor1, faktor2)) {
    override fun kjør(opplysninger: LesbarOpplysninger): R {
        val a = opplysninger.finnOpplysning(faktor1)
        val b = opplysninger.finnOpplysning(faktor2)
        return operasjon(a.verdi, b.verdi)
    }

    override fun toString(): String = "Multiplikasjon av $faktor1 med $faktor2"
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

@JvmName("multiplikasjonBeløpInt")
fun Opplysningstype<Beløp>.multiplikasjon(
    beløp: Opplysningstype<Beløp>,
    faktor: Opplysningstype<Int>,
) = Multiplikasjon(
    this,
    beløp,
    faktor,
) { a, b -> a * b }

@JvmName("multiplikasjonInt")
fun Opplysningstype<Int>.multiplikasjon(
    faktor1: Opplysningstype<Int>,
    faktor2: Opplysningstype<Int>,
) = Multiplikasjon(
    this,
    faktor1,
    faktor2,
) { a, b -> a * b }
