package no.nav.dagpenger.opplysning

import no.nav.dagpenger.uuid.UUIDv7
import java.time.LocalDate
import java.util.UUID

class Opplysninger private constructor(
    override val id: UUID,
    opplysninger: List<Opplysning<*>> = emptyList(),
    basertPå: List<Opplysninger> = emptyList(),
) : LesbarOpplysninger {
    private val opplysninger: MutableList<Opplysning<*>> = opplysninger.toMutableList()
    private val basertPåOpplysninger: List<Opplysning<*>> = basertPå.flatMap { it.basertPåOpplysninger + it.opplysninger }.toList()
    private val alleOpplysninger: List<Opplysning<*>> get() = (basertPåOpplysninger + opplysninger).filterNot { it.erErstattet }

    constructor() : this(UUIDv7.ny(), emptyList(), emptyList())
    constructor(id: UUID, opplysninger: List<Opplysning<*>>) : this(id, opplysninger, emptyList())
    constructor(opplysninger: List<Opplysning<*>>, basertPå: List<Opplysninger> = emptyList()) : this(UUIDv7.ny(), opplysninger, basertPå)
    constructor(vararg basertPå: Opplysninger) : this(emptyList(), basertPå.toList())

    val aktiveOpplysninger get() = opplysninger.toList()

    override fun forDato(gjelderFor: LocalDate): LesbarOpplysninger {
        // TODO: Erstatt med noe collectorgreier får å unngå at opplysninger som er erstattet blir med
        val opplysningerForDato = opplysninger.filter { it.gyldighetsperiode.inneholder(gjelderFor) }.filterNot { it.erErstattet }
        return Opplysninger(UUIDv7.ny(), opplysningerForDato)
    }

    @Suppress("UNCHECKED_CAST")
    fun <T : Comparable<T>> leggTil(opplysning: Opplysning<T>) {
        val erstattes: Opplysning<T>? = alleOpplysninger.find { it.overlapper(opplysning) } as Opplysning<T>?
        if (erstattes !== null) {
            if (opplysning.overlapperHalenAv(erstattes)) {
                // Overlapp på halen av eksisterende opplysning
                val forkortet = erstattes.lagErstatning(opplysning)
                opplysninger.add(forkortet)
                opplysninger.add(opplysning)
            } else if (erstattes.harSammegyldighetsperiode(opplysning)) {
                // Overlapp for samme periode
                opplysninger.addAll(erstattes.erstattesAv(opplysning))
            } else if (opplysning.starterFørOgOverlapper(erstattes)) {
                // Overlapp på starten av eksisterende opplysning
                erstattes.erstattesAv(opplysning)
                opplysninger.add(opplysning)
            } else {
                throw IllegalArgumentException("Kan ikke legge til opplysning som overlapper med eksisterende opplysning")
            }
        } else {
            opplysninger.add(opplysning)
        }
    }

    internal fun <T : Comparable<T>> leggTilUtledet(opplysning: Opplysning<T>) {
        alleOpplysninger.find { it.overlapper(opplysning) }?.let {
            val erstattet = it as Opplysning<T>
            opplysninger.addAll(erstattet.erstattesAv(opplysning))
        } ?: opplysninger.add(opplysning)
    }

    override fun <T : Comparable<T>> finnOpplysning(opplysningstype: Opplysningstype<T>): Opplysning<T> =
        finnNullableOpplysning(opplysningstype) ?: throw IllegalStateException("Har ikke opplysning $opplysningstype som er gyldig")

    override fun finnOpplysning(opplysningId: UUID) =
        opplysninger.singleOrNull { it.id == opplysningId } ?: throw IllegalStateException("Har ikke opplysning $opplysningId")

    override fun har(opplysningstype: Opplysningstype<*>) = finnNullableOpplysning(opplysningstype) != null

    override fun finnAlle(opplysningstyper: List<Opplysningstype<*>>) = opplysningstyper.mapNotNull { finnNullableOpplysning(it) }

    override fun finnAlle() = alleOpplysninger.toList()

    @Suppress("UNCHECKED_CAST")
    private fun <T : Comparable<T>> finnNullableOpplysning(opplysningstype: Opplysningstype<T>): Opplysning<T>? =
        alleOpplysninger.singleOrNull { it.er(opplysningstype) } as Opplysning<T>?

    private fun <T : Comparable<T>> Opplysning<T>.overlapperHalenAv(opplysning: Opplysning<T>) =
        this.gyldighetsperiode.fom.isAfter(opplysning.gyldighetsperiode.fom) &&
            this.gyldighetsperiode.fom <= opplysning.gyldighetsperiode.tom

    private fun <T : Comparable<T>> Opplysning<T>.harSammegyldighetsperiode(opplysning: Opplysning<T>) =
        this.gyldighetsperiode == opplysning.gyldighetsperiode

    private fun <T : Comparable<T>> Opplysning<T>.starterFørOgOverlapper(opplysning: Opplysning<T>) =
        this.gyldighetsperiode.fom.isBefore(opplysning.gyldighetsperiode.fom) &&
            opplysning.gyldighetsperiode.inneholder(this.gyldighetsperiode.tom)

    operator fun plus(tidligereOpplysninger: List<Opplysninger>) = Opplysninger(id, opplysninger, tidligereOpplysninger)
}
