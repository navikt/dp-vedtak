package no.nav.dagpenger.opplysning.regel

import no.nav.dagpenger.opplysning.Faktum
import no.nav.dagpenger.opplysning.Gyldighetsperiode
import no.nav.dagpenger.opplysning.Hypotese
import no.nav.dagpenger.opplysning.LesbarOpplysninger
import no.nav.dagpenger.opplysning.Opplysning
import no.nav.dagpenger.opplysning.Opplysningstype
import no.nav.dagpenger.opplysning.Utledning

abstract class Regel<T : Comparable<T>> internal constructor(
    internal val produserer: Opplysningstype<T>,
    internal val avhengerAv: List<Opplysningstype<*>> = emptyList(),
) {
    internal open fun kanKjøre(opplysninger: LesbarOpplysninger): Boolean {
        val avhengigheter = opplysninger.finnAlle(avhengerAv)

        if (avhengigheter.size != avhengerAv.size) return false
        if (avhengigheter.erIkkeSammenhengende()) return false

        // Har vi produsert produkt av disse opplysningene før?
        val produkter = opplysninger.finnAlle().filter { it.er(produserer) }

        if (produkter.isEmpty()) return true

        return produkter.none { produkt ->
            // Har avhengigheten blitt erstattet og produktet vi allerede har skal oppdateres?
            avhengigheter.any { it in produkt.utledetAv?.opplysninger.orEmpty() }
        }
    }

    private fun List<Opplysning<*>>.erIkkeSammenhengende() =
        sortedBy { it.gyldighetsperiode.fom }
            .zipWithNext()
            .any { (forrige, neste) -> neste.gyldighetsperiode.fom.isAfter(forrige.gyldighetsperiode.tom) }

    abstract override fun toString(): String

    protected abstract fun kjør(opplysninger: LesbarOpplysninger): T

    fun produserer(opplysningstype: Opplysningstype<*>) = produserer.er(opplysningstype)

    internal fun lagProdukt(opplysninger: LesbarOpplysninger): Opplysning<T> {
        val basertPå = opplysninger.finnAlle(avhengerAv)
        val erAlleFaktum = basertPå.all { it is Faktum<*> }
        val utledetAv = Utledning(this, basertPå)
        val gyldig =
            Gyldighetsperiode(
                fom = basertPå.maxOf { it.gyldighetsperiode.fom },
                tom = basertPå.minOf { it.gyldighetsperiode.tom },
            )
        return when (erAlleFaktum) {
            true -> Faktum(opplysningstype = produserer, verdi = kjør(opplysninger), utledetAv = utledetAv, gyldighetsperiode = gyldig)
            false -> Hypotese(opplysningstype = produserer, verdi = kjør(opplysninger), utledetAv = utledetAv, gyldighetsperiode = gyldig)
        }
    }
}
