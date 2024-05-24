package no.nav.dagpenger.opplysning

import no.nav.dagpenger.opplysning.verdier.Ulid
import java.time.LocalDate

interface Klassifiserbart {
    fun er(type: Opplysningstype<*>): Boolean
}

fun String.id(id: String) = OpplysningTypeId(id, this)

data class OpplysningTypeId(val id: String, val beskrivelse: String)

class Opplysningstype<T : Comparable<T>>(
    private val opplysningTypeId: OpplysningTypeId,
    val datatype: Datatype<T>,
    private val parent: Opplysningstype<T>? = null,
    private val child: MutableSet<Opplysningstype<*>> = mutableSetOf(),
) : Klassifiserbart {
    constructor(
        navn: String,
        datatype: Datatype<T>,
        parent: Opplysningstype<T>? = null,
    ) : this(OpplysningTypeId(navn, navn), datatype, parent)

    val id = opplysningTypeId.id
    val navn = opplysningTypeId.beskrivelse

    init {
        registrer(this)
        parent?.child?.add(this)
    }

    companion object {
        private val typer = mutableSetOf<Opplysningstype<*>>()

        fun finn(predikat: (Opplysningstype<*>) -> Boolean): Opplysningstype<*> {
            return typer.single { predikat(it) }
        }

        fun finn(id: String): Opplysningstype<*> {
            return finn { it.id == id }
        }

        fun <T : Comparable<T>> finn(opplysningTypeId: OpplysningTypeId): Opplysningstype<T> {
            return (
                typer.find { it.opplysningTypeId == opplysningTypeId }
                    ?: throw IllegalArgumentException("Fant ikke opplysningstype $opplysningTypeId")
            ) as Opplysningstype<T>
        }

        private fun registrer(opplysningstype: Opplysningstype<*>) {
            // @todo: Vi trenger denne sjekken men krever en del refaktorering av testene
            require(!typer.contains(opplysningstype)) { "Opplysningstype $opplysningstype er allerede registrert" }
            typer.add(opplysningstype)
        }

        fun somHeltall(
            opplysningTypeId: OpplysningTypeId,
            parent: Opplysningstype<Int>? = null,
        ) = Opplysningstype(opplysningTypeId, Heltall, parent)

        fun somHeltall(
            navn: String,
            parent: Opplysningstype<Int>? = null,
        ) = somHeltall(navn.id(navn), parent)

        fun somDesimaltall(
            opplysningTypeId: OpplysningTypeId,
            parent: Opplysningstype<Double>? = null,
        ) = Opplysningstype(opplysningTypeId, Desimaltall, parent)

        fun somDesimaltall(
            navn: String,
            parent: Opplysningstype<Double>? = null,
        ) = somDesimaltall(navn.id(navn), parent)

        fun somDato(
            opplysningTypeId: OpplysningTypeId,
            parent: Opplysningstype<LocalDate>? = null,
        ) = Opplysningstype(opplysningTypeId, Dato, parent)

        fun somDato(
            navn: String,
            parent: Opplysningstype<LocalDate>? = null,
        ) = somDato(navn.id(navn), parent)

        fun somUlid(
            opplysningTypeId: OpplysningTypeId,
            parent: Opplysningstype<Ulid>? = null,
        ) = Opplysningstype(opplysningTypeId, ULID, parent)

        fun somUlid(
            navn: String,
            parent: Opplysningstype<Ulid>? = null,
        ) = somUlid(navn.id(navn), parent)

        fun somBoolsk(
            opplysningTypeId: OpplysningTypeId,
            parent: Opplysningstype<Boolean>? = null,
        ) = Opplysningstype(opplysningTypeId, Boolsk, parent)

        fun somBoolsk(
            navn: String,
            parent: Opplysningstype<Boolean>? = null,
        ) = somBoolsk(navn.id(navn), parent)
    }

    override infix fun er(type: Opplysningstype<*>): Boolean {
        return opplysningTypeId == type.opplysningTypeId || parent?.er(type) ?: false
    }

    override fun toString() = "opplysning om $navn"

    override fun equals(other: Any?): Boolean = other is Opplysningstype<*> && other.opplysningTypeId == this.opplysningTypeId

    override fun hashCode() = opplysningTypeId.hashCode() * 31
}
