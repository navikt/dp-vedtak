package no.nav.dagpenger.opplysning

import java.util.UUID

interface LesbarOpplysninger {
    val id: UUID

    fun <T : Comparable<T>> finnOpplysning(opplysningstype: Opplysningstype<T>): Opplysning<T>

    fun har(opplysningstype: Opplysningstype<*>): Boolean

    fun finnAlle(opplysningstyper: List<Opplysningstype<*>>): List<Opplysning<*>>

    fun finnAlle(): List<Opplysning<*>>

    fun finnOpplysning(opplysningId: UUID): Opplysning<*>
}

class Opplysninger private constructor(
    override val id: UUID,
    opplysninger: List<Opplysning<*>> = emptyList(),
    basertPå: List<Opplysninger> = emptyList(),
) : LesbarOpplysninger {
    private lateinit var regelkjøring: Regelkjøring
    private val opplysninger: MutableList<Opplysning<*>> = opplysninger.toMutableList()
    private val basertPåOpplysninger: List<Opplysning<*>> = basertPå.flatMap { it.basertPåOpplysninger + it.opplysninger }.toList()
    private val alleOpplysninger: List<Opplysning<*>> get() = basertPåOpplysninger + opplysninger

    constructor() : this(UUIDv7.ny(), emptyList(), emptyList())
    constructor(id: UUID, opplysninger: List<Opplysning<*>>) : this(id, opplysninger, emptyList())
    constructor(opplysninger: List<Opplysning<*>>, basertPå: List<Opplysninger> = emptyList()) : this(UUIDv7.ny(), opplysninger, basertPå)
    constructor(vararg basertPå: Opplysninger) : this(emptyList(), basertPå.toList())

    fun registrer(regelkjøring: Regelkjøring) {
        this.regelkjøring = regelkjøring
    }

    internal fun leggTilUtledet(opplysning: Opplysning<*>) {
        alleOpplysninger.find { it.overlapper(opplysning) }?.let {
            opplysninger.remove(it)
        }
        opplysninger.add(opplysning)
        regelkjøring.evaluer()
    }

    fun leggTil(opplysning: Opplysning<*>) {
        require(alleOpplysninger.none { it.overlapper(opplysning) }) {
            """Opplysning ${opplysning.opplysningstype} finnes allerede med overlappende gyldighetsperiode.
               Opplysning som legges til: ${opplysning.gyldighetsperiode}
               Opplysning som allerede finnes: ${
                alleOpplysninger.first {
                    it.overlapper(
                        opplysning,
                    )
                }.gyldighetsperiode
            }"""
        }
        opplysninger.add(opplysning)
        regelkjøring.evaluer()
    }

    fun erstatt(
        opplysning: Opplysning<*>,
        med: Opplysning<*>,
    ) {
        require(alleOpplysninger.contains(opplysning)) {
            "Opplysning ${opplysning.opplysningstype} finnes ikke. Kan ikke erstatte noe som ikke finnes."
        }
        require(opplysning.overlapper(med)) {
            """Opplysning ${opplysning.opplysningstype} og ${med.opplysningstype} overlapper ikke. 
                |Kan ikke erstatte med en opplysning som ikke er lik.
            """.trimMargin()
        }
        opplysninger.remove(opplysning)
        opplysninger.add(med)
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
}
