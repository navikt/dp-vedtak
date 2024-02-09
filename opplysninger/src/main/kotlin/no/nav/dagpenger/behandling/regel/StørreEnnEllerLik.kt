package no.nav.dagpenger.behandling.regel

import no.nav.dagpenger.behandling.LesbarOpplysninger
import no.nav.dagpenger.behandling.Opplysningstype

class StørreEnnEllerLik internal constructor(
    produserer: Opplysningstype<Boolean>,
    private val a: Opplysningstype<Double>,
    private val b: Opplysningstype<Double>,
) : Regel<Boolean>(produserer, listOf(a, b)) {
    override fun kjør(opplysninger: LesbarOpplysninger): Boolean {
        val a = opplysninger.finnOpplysning(a).verdi
        val b = opplysninger.finnOpplysning(b).verdi
        return a >= b
    }

    override fun toString() = "Større enn $a >= $b"
}

fun Opplysningstype<Boolean>.størreEnnEllerLik(
    er: Opplysningstype<Double>,
    størreEnn: Opplysningstype<Double>,
) = StørreEnnEllerLik(this, er, størreEnn)
