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
    internal open fun kanKjøre(opplysninger: LesbarOpplysninger): Boolean = opplysninger.finnAlle(avhengerAv).size == avhengerAv.size

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
            true -> Faktum(produserer, kjør(opplysninger), utledetAv = utledetAv, gyldighetsperiode = gyldig)
            false -> Hypotese(produserer, kjør(opplysninger), utledetAv = utledetAv, gyldighetsperiode = gyldig)
        }
    }
}
