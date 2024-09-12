package no.nav.dagpenger.opplysning.regel

import no.nav.dagpenger.opplysning.LesbarOpplysninger
import no.nav.dagpenger.opplysning.Opplysningstype

class Ekstern<T : Comparable<T>> internal constructor(
    produserer: Opplysningstype<T>,
    avhengigheter: List<Opplysningstype<*>>,
) : Regel<T>(produserer, avhengigheter) {
    override fun kanKjøre(opplysninger: LesbarOpplysninger) = false

    override fun kjør(opplysninger: LesbarOpplysninger): T = throw IllegalStateException("Kan ikke kjøres")

    override fun toString() = "Henter inn opplysning for $produserer med ${avhengerAv.joinToString { it.navn }} som avhengigheter."
}

fun <T : Comparable<T>> Opplysningstype<T>.innhentMed(vararg opplysninger: Opplysningstype<*>) = Ekstern(this, opplysninger.toList())

val <T : Comparable<T>> Opplysningstype<T>.innhentes get() = Ekstern(this, emptyList())
