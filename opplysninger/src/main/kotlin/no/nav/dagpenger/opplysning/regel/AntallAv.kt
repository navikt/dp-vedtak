package no.nav.dagpenger.opplysning.regel

import no.nav.dagpenger.opplysning.LesbarOpplysninger
import no.nav.dagpenger.opplysning.Opplysningstype

class AntallAv<T : Comparable<T>>(
    produserer: Opplysningstype<Int>,
    val opplysningstype: Opplysningstype<T>,
    val filter: T.() -> Boolean,
) : Regel<Int>(produserer, listOf(opplysningstype)) {
    @Suppress("UNCHECKED_CAST")
    override fun kjør(opplysninger: LesbarOpplysninger): Int {
        val opplysninge = opplysninger.finnAlle().filter { it.er(opplysningstype) }
        return opplysninge.filter { filter(it.verdi as T) }.size
    }

    override fun toString() = "Produserer $produserer ved å telle antall instanser av $opplysningstype som oppfyller filteret."
}

fun <T : Comparable<T>> Opplysningstype<Int>.antallAv(
    opplysningstype: Opplysningstype<T>,
    filter: T.() -> Boolean,
) = AntallAv(this, opplysningstype, filter)
