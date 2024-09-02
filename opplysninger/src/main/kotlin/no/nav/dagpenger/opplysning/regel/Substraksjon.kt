package no.nav.dagpenger.opplysning.regel

import no.nav.dagpenger.opplysning.LesbarOpplysninger
import no.nav.dagpenger.opplysning.Opplysningstype

class Substraksjon<T : Comparable<T>> internal constructor(
    produserer: Opplysningstype<T>,
    private vararg val opplysningstyper: Opplysningstype<T>,
    private val operasjon: (T, T) -> T,
) : Regel<T>(produserer, opplysningstyper.toList()) {
    override fun kjør(opplysninger: LesbarOpplysninger): T {
        val verdier = opplysninger.finnAlle(opplysningstyper.toList()).map { it.verdi as T }
        return verdier.reduce(operasjon)
    }

    override fun toString() = "Substraksjon av ${opplysningstyper.joinToString(", ")}"
}

@JvmName("substraksjonDouble")
fun Opplysningstype<Double>.substraksjon(vararg opplysningstype: Opplysningstype<Double>) =
    Substraksjon(this, *opplysningstype) { a, b -> a - b }

@JvmName("substraksjonInt")
fun Opplysningstype<Int>.substraksjon(vararg opplysningstype: Opplysningstype<Int>) = Substraksjon(this, *opplysningstype) { a, b -> a - b }
