package no.nav.dagpenger.opplysning

import java.util.UUID

class Opplysninger private constructor(
    override val id: UUID,
    opplysninger: List<Opplysning<*>> = emptyList(),
    val basertPå: List<Opplysninger> = emptyList(),
) : LesbarOpplysninger {
    private lateinit var regelkjøring: Regelkjøring
    private val opplysninger: MutableList<Opplysning<*>> = opplysninger.toMutableList()
    private val basertPåOpplysninger: List<Opplysning<*>> = basertPå.flatMap { it.basertPåOpplysninger + it.opplysninger }.toList()
    private val alleOpplysninger: List<Opplysning<*>> get() = (basertPåOpplysninger + opplysninger).filterNot { it.erErstattet }

    constructor() : this(UUIDv7.ny(), emptyList(), emptyList())
    constructor(id: UUID, opplysninger: List<Opplysning<*>>) : this(id, opplysninger, emptyList())
    constructor(opplysninger: List<Opplysning<*>>, basertPå: List<Opplysninger> = emptyList()) : this(UUIDv7.ny(), opplysninger, basertPå)
    constructor(vararg basertPå: Opplysninger) : this(emptyList(), basertPå.toList())

    fun registrer(regelkjøring: Regelkjøring) {
        this.regelkjøring = regelkjøring
    }

    fun <T : Comparable<T>> leggTil(opplysning: Opplysning<T>) {
        val erstattes: Opplysning<T>? = alleOpplysninger.find { it.overlapper(opplysning) } as Opplysning<T>?
        if (erstattes !== null) {
            if (erstattes.erFør(opplysning) && opplysning.etterEllerLik(erstattes)) {
                // Overlapp på halen
                opplysninger.addAll(erstattes.erstattesAv(opplysning))
            } else if (erstattes.harSammegyldighetsperiode(opplysning)) {
                // Overlapp for samme periode
                opplysninger.addAll(erstattes.erstattesAv(opplysning))
            } else {
                throw IllegalArgumentException("Kan ikke legge til opplysning som overlapper med eksisterende opplysning")
            }
        } else {
            opplysninger.add(opplysning)
        }

        regelkjøring.evaluer()
    }

    private fun <T : Comparable<T>> Opplysning<T>.erFør(opplysning: Opplysning<T>) =
        this.gyldighetsperiode.fom.isBefore(opplysning.gyldighetsperiode.fom)

    private fun <T : Comparable<T>> Opplysning<T>.etterEllerLik(opplysning: Opplysning<T>) =
        this.gyldighetsperiode.tom >= opplysning.gyldighetsperiode.tom

    private fun <T : Comparable<T>> Opplysning<T>.harSammegyldighetsperiode(opplysning: Opplysning<T>) =
        this.gyldighetsperiode.tom == opplysning.gyldighetsperiode.tom

    internal fun leggTilUtledet(opplysning: Opplysning<*>) {
        alleOpplysninger.find { it.overlapper(opplysning) }?.let {
            opplysninger.remove(it)
        }
        opplysninger.add(opplysning)
        regelkjøring.evaluer()
    }

    override fun <T : Comparable<T>> finnOpplysning(opplysningstype: Opplysningstype<T>): Opplysning<T> {
        return finnNullableOpplysning(opplysningstype)
            ?: throw IllegalStateException("Har ikke opplysning $opplysningstype som er gyldig for ${regelkjøring.forDato}")
    }

    override fun finnOpplysning(opplysningId: UUID) =
        opplysninger.firstOrNull { it.id == opplysningId } ?: throw IllegalStateException("Har ikke opplysning $opplysningId")

    override fun har(opplysningstype: Opplysningstype<*>) = finnNullableOpplysning(opplysningstype) != null

    override fun finnAlle(opplysningstyper: List<Opplysningstype<*>>) = opplysningstyper.mapNotNull { finnNullableOpplysning(it) }

    override fun finnAlle() = alleOpplysninger.toList()

    fun aktiveOpplysninger() = opplysninger.toList()

    @Suppress("UNCHECKED_CAST")
    private fun <T : Comparable<T>> finnNullableOpplysning(opplysningstype: Opplysningstype<T>): Opplysning<T>? =
        alleOpplysninger.firstOrNull { it.er(opplysningstype) && it.gyldighetsperiode.inneholder(regelkjøring.forDato) } as Opplysning<T>?

    operator fun plus(tidligereOpplysninger: List<Opplysninger>) = Opplysninger(id, opplysninger, tidligereOpplysninger)

    operator fun plus(tidligereOpplysninger: Opplysninger) = Opplysninger(id, opplysninger, listOf(tidligereOpplysninger))
}
