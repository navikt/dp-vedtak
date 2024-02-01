package no.nav.dagpenger.behandling.regel

import no.nav.dagpenger.behandling.Faktum
import no.nav.dagpenger.behandling.Hypotese
import no.nav.dagpenger.behandling.LesbarOpplysninger
import no.nav.dagpenger.behandling.Opplysning
import no.nav.dagpenger.behandling.Opplysningstype
import no.nav.dagpenger.behandling.Utledning

abstract class Regel<T : Comparable<T>>(
    internal val produserer: Opplysningstype<T>,
    val avhengerAv: List<Opplysningstype<*>> = emptyList(),
) {
    fun kanKjøre(opplysninger: LesbarOpplysninger): Boolean = opplysninger.finnAlle(avhengerAv).size == avhengerAv.size

    protected abstract fun kjør(opplysninger: LesbarOpplysninger): T

    fun produserer(opplysningstype: Opplysningstype<*>) = produserer.er(opplysningstype)

    fun lagProdukt(opplysninger: LesbarOpplysninger): Opplysning<T> {
        val basertPå = opplysninger.finnAlle(avhengerAv)
        val erAlleFaktum =
            basertPå.all {
                it is Faktum<*>
            }
        return when (erAlleFaktum) {
            true -> return Faktum(produserer, kjør(opplysninger), utledetAv = Utledning(this, basertPå))
            false -> Hypotese(produserer, kjør(opplysninger), utledetAv = Utledning(this, basertPå))
        }
    }
}
