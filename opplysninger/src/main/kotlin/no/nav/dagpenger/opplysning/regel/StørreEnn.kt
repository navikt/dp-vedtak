package no.nav.dagpenger.opplysning.regel

import no.nav.dagpenger.opplysning.LesbarOpplysninger
import no.nav.dagpenger.opplysning.Opplysningstype

class StørreEnn internal constructor(
    produserer: Opplysningstype<Boolean>,
    private val a: Opplysningstype<Double>,
    private val b: Opplysningstype<Double>,
) : Regel<Boolean>(produserer, listOf(a, b)) {
    override fun kjør(opplysninger: LesbarOpplysninger): Boolean {
        val a = opplysninger.finnOpplysning(a).verdi
        val b = opplysninger.finnOpplysning(b).verdi
        return a > b
    }

    override fun toString() = "Større enn $a > $b"
}

fun Opplysningstype<Boolean>.størreEnn(
    er: Opplysningstype<Double>,
    størreEnn: Opplysningstype<Double>,
) = StørreEnn(this, er, størreEnn)
