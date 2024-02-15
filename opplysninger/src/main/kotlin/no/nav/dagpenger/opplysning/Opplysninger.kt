package no.nav.dagpenger.opplysning

interface LesbarOpplysninger {
    fun <T : Comparable<T>> finnOpplysning(opplysningstype: Opplysningstype<T>): Opplysning<T>

    fun har(opplysningstype: Opplysningstype<*>): Boolean

    fun finnAlle(opplysningstyper: List<Opplysningstype<*>>): List<Opplysning<*>>

    fun finnAlle(): List<Opplysning<*>>
}

class Opplysninger(
    opplysninger: List<Opplysning<*>> = emptyList(),
) : LesbarOpplysninger {
    private lateinit var regelkjøring: Regelkjøring
    private val opplysninger: MutableList<Opplysning<*>> = opplysninger.toMutableList()

    fun opplysninger(): List<Opplysning<*>> = opplysninger.toList()

    constructor() : this(mutableListOf())

    fun registrer(regelkjøring: Regelkjøring) {
        this.regelkjøring = regelkjøring
    }

    override fun <T : Comparable<T>> finnOpplysning(opplysningstype: Opplysningstype<T>): Opplysning<T> {
        return finnNullableOpplysning(opplysningstype)
            ?: throw IllegalStateException("Har ikke opplysning $opplysningstype som er gyldig for ${regelkjøring.forDato}")
    }

    fun leggTil(opplysning: Opplysning<*>) {
        require(opplysninger.none { it.sammeSom(opplysning) }) {
            "Opplysning ${opplysning.opplysningstype} finnes allerede med overlappende gyldighetsperiode"
        }
        opplysninger.add(opplysning)
        regelkjøring.evaluer()
    }

    private fun <T : Comparable<T>> finnNullableOpplysning(opplysningstype: Opplysningstype<T>): Opplysning<T>? =
        opplysninger.firstOrNull { it.er(opplysningstype) && it.gyldighetsperiode.inneholder(regelkjøring.forDato) } as Opplysning<T>?

    override fun har(opplysningstype: Opplysningstype<*>) = finnNullableOpplysning(opplysningstype) != null

    override fun finnAlle(opplysningstyper: List<Opplysningstype<*>>) = opplysningstyper.mapNotNull { finnNullableOpplysning(it) }

    override fun finnAlle() = opplysninger.toList()
}
