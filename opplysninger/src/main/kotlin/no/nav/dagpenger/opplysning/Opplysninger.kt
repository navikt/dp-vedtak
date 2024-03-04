package no.nav.dagpenger.opplysning

interface LesbarOpplysninger {
    fun <T : Comparable<T>> finnOpplysning(opplysningstype: Opplysningstype<T>): Opplysning<T>

    fun har(opplysningstype: Opplysningstype<*>): Boolean

    fun finnAlle(opplysningstyper: List<Opplysningstype<*>>): List<Opplysning<*>>

    fun finnAlle(): List<Opplysning<*>>
}

class Opplysninger(
    opplysninger: List<Opplysning<*>> = emptyList(),
    basertPå: List<Opplysninger> = emptyList(),
) : LesbarOpplysninger {
    private lateinit var regelkjøring: Regelkjøring
    private val opplysninger: MutableList<Opplysning<*>> = opplysninger.toMutableList()
    private val basertPåOpplysninger: List<Opplysning<*>> = basertPå.flatMap { it.opplysninger }.toList()
    private val alleOpplysninger: List<Opplysning<*>> get() = basertPåOpplysninger + opplysninger

    constructor() : this(mutableListOf(), emptyList())
    constructor(vararg basertPå: Opplysninger) : this(emptyList(), basertPå.toList())
    constructor(basertPå: List<Opplysninger>) : this(emptyList(), basertPå)

    fun registrer(regelkjøring: Regelkjøring) {
        this.regelkjøring = regelkjøring
    }

    fun leggTil(opplysning: Opplysning<*>) {
        require(alleOpplysninger.none { it.sammeSom(opplysning) }) {
            "Opplysning ${opplysning.opplysningstype} finnes allerede med overlappende gyldighetsperiode"
        }
        opplysninger.add(opplysning)
        regelkjøring.evaluer()
    }

    override fun <T : Comparable<T>> finnOpplysning(opplysningstype: Opplysningstype<T>): Opplysning<T> {
        return finnNullableOpplysning(opplysningstype)
            ?: throw IllegalStateException("Har ikke opplysning $opplysningstype som er gyldig for ${regelkjøring.forDato}")
    }

    override fun har(opplysningstype: Opplysningstype<*>) = finnNullableOpplysning(opplysningstype) != null

    override fun finnAlle(opplysningstyper: List<Opplysningstype<*>>) = opplysningstyper.mapNotNull { finnNullableOpplysning(it) }

    override fun finnAlle() = alleOpplysninger.toList()

    private fun <T : Comparable<T>> finnNullableOpplysning(opplysningstype: Opplysningstype<T>): Opplysning<T>? =
        alleOpplysninger.firstOrNull { it.er(opplysningstype) && it.gyldighetsperiode.inneholder(regelkjøring.forDato) } as Opplysning<T>?
}
