package no.nav.dagpenger.opplysning.regel

import no.nav.dagpenger.opplysning.LesbarOpplysninger
import no.nav.dagpenger.opplysning.Opplysningstype
import no.nav.dagpenger.opplysning.verdier.Beløp

class MindreEnn<T : Comparable<T>> internal constructor(
    produserer: Opplysningstype<Boolean>,
    private val a: Opplysningstype<T>,
    private val b: Opplysningstype<T>,
) : Regel<Boolean>(produserer, listOf(a, b)) {
    override fun kjør(opplysninger: LesbarOpplysninger): Boolean {
        val a = opplysninger.finnOpplysning(a).verdi
        val b = opplysninger.finnOpplysning(b).verdi
        return a < b
    }

    override fun toString() = "Sjekker om $a er mindre enn $b"
}

@JvmName("mindreEnnDouble")
fun Opplysningstype<Boolean>.mindreEnn(
    er: Opplysningstype<Double>,
    mindreEnn: Opplysningstype<Double>,
) = StørreEnn(this, er, mindreEnn)

@JvmName("mindreEnnBeløp")
fun Opplysningstype<Boolean>.mindreEnn(
    er: Opplysningstype<Beløp>,
    mindreEnn: Opplysningstype<Beløp>,
) = StørreEnn(this, er, mindreEnn)
