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
    internal open fun lagPlan(
        opplysninger: LesbarOpplysninger,
        plan: MutableSet<Regel<*>>,
        gjeldendeRegler: List<Regel<*>>,
    ) {
        if (opplysninger.har(produserer)) {
            val produkt = opplysninger.finnOpplysning(produserer)
            if (produkt.utledetAv == null) {
                return
            }

            produkt.utledetAv.opplysninger.forEach { avhengighet ->
                val produsent =
                    gjeldendeRegler.singleOrNull { it.produserer(avhengighet.opplysningstype) }
                        ?: throw IllegalStateException("Fant ikke produsent for $avhengighet")
                produsent.lagPlan(opplysninger, plan, gjeldendeRegler)
            }

            val avhengighetErErstattet = produkt.utledetAv.opplysninger.any { it.erErstattet }
            if (avhengighetErErstattet) {
                plan.add(this)
                return
            }
        } else {
            val avhengigheter = opplysninger.finnAlle(avhengerAv)

            if (avhengigheter.size == avhengerAv.size) {
                plan.add(this)
                return
            } else {
                avhengerAv.forEach { avhengighet ->
                    val produsent =
                        gjeldendeRegler.singleOrNull { it.produserer(avhengighet) }
                            ?: throw IllegalStateException("Fant ikke produsent for $avhengighet")
                    produsent.lagPlan(opplysninger, plan, gjeldendeRegler)
                }
            }
        }
        return
    }

    fun avhengighetErErstattet(produkt: Opplysning<*>): Boolean {
        // Recursively check if any opplysning in the chain is replaced
        fun isReplacedRecursively(opplysning: Opplysning<*>): Boolean {
            // Check if this opplysning is replaced or any of its dependencies are replaced
            return opplysning.erErstattet ||
                (opplysning.utledetAv?.opplysninger?.any { isReplacedRecursively(it) } ?: false)
        }

        // Start the recursive check with the provided produkt
        return isReplacedRecursively(produkt)
    }

    abstract override fun toString(): String

    protected abstract fun kjør(opplysninger: LesbarOpplysninger): T

    fun produserer(opplysningstype: Opplysningstype<*>) = produserer.er(opplysningstype)

    internal fun lagProdukt(opplysninger: LesbarOpplysninger): Opplysning<T> {
        if (avhengerAv.isEmpty()) return Faktum(produserer, kjør(opplysninger))

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
            false ->
                Hypotese(
                    opplysningstype = produserer,
                    verdi = kjør(opplysninger),
                    utledetAv = utledetAv,
                    gyldighetsperiode = gyldig,
                )
        }
    }
}
