package no.nav.dagpenger.opplysning

import java.time.LocalDate

interface Klassifiserbart {
    fun er(type: Opplysningstype<*>): Boolean
}

fun String.id(id: String) = OpplysningTypeId(id, this)

data class OpplysningTypeId(val id: String, val beskrivelse: String)

class Opplysningstype<T : Comparable<T>> private constructor(
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

    companion object {
        val typer = mutableListOf<Opplysningstype<*>>()

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

        fun somBoolsk(
            opplysningTypeId: OpplysningTypeId,
            parent: Opplysningstype<Boolean>? = null,
        ) = Opplysningstype(opplysningTypeId, Boolsk, parent)

        fun somBoolsk(
            navn: String,
            parent: Opplysningstype<Boolean>? = null,
        ) = somBoolsk(navn.id(navn), parent)
    }

    init {
        typer.add(this)
    }

    val id = opplysningTypeId.id
    val navn = opplysningTypeId.beskrivelse

    init {
        parent?.child?.add(this)
    }

    override infix fun er(type: Opplysningstype<*>): Boolean {
        return opplysningTypeId == type.opplysningTypeId || parent?.er(type) ?: false
    }

    override fun toString(): String {
        // TODO: Det er noe muffens med hierarki og data class
        return "Opplysningstype(navn='$navn', parent=${parent?.navn}, child=${child.size})"
    }

    override fun equals(other: Any?): Boolean = other is Opplysningstype<*> && other.opplysningTypeId == this.opplysningTypeId

    override fun hashCode() = navn.hashCode() * 31
}
