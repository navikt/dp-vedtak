package no.nav.dagpenger.opplysning.regel

import no.nav.dagpenger.opplysning.LesbarOpplysninger
import no.nav.dagpenger.opplysning.Opplysningstype

class Ekstern<T : Comparable<T>> internal constructor(
    produserer: Opplysningstype<T>,
    avhengigheter: List<Opplysningstype<*>>,
) : Regel<T>(produserer, avhengigheter) {
    override fun kanKjøre(opplysninger: LesbarOpplysninger) = false

    override fun kjør(opplysninger: LesbarOpplysninger): T = throw IllegalStateException("Kan ikke kjøres")

    override fun toString() = "Ekstern innhenting for $produserer"
}

fun <T : Comparable<T>> Opplysningstype<T>.innhentMed(vararg opplysninger: Opplysningstype<*>) = Ekstern(this, opplysninger.toList())
