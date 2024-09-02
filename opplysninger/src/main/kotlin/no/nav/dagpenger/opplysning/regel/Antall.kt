package no.nav.dagpenger.opplysning.regel

import no.nav.dagpenger.opplysning.LesbarOpplysninger
import no.nav.dagpenger.opplysning.Opplysningstype

class Antall<T : Comparable<T>> internal constructor(
    produserer: Opplysningstype<Int>,
    private val type: Opplysningstype<T>,
) : Regel<Int>(produserer, listOf(type)) {
    override fun kjør(opplysninger: LesbarOpplysninger): Int = opplysninger.finnAlle().count { it.opplysningstype == type }

    override fun toString() = "Hvor mange opplysninger finnes av type $type"
}

fun Opplysningstype<Int>.antall(type: Opplysningstype<*>) = Antall(this, type)
