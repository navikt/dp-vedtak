package no.nav.dagpenger.opplysning

import no.nav.dagpenger.opplysning.verdier.Beløp
import no.nav.dagpenger.opplysning.verdier.Stønadsperiode
import no.nav.dagpenger.opplysning.verdier.Ulid
import java.time.LocalDate

interface Klassifiserbart {
    fun er(type: Opplysningstype<*>): Boolean
}

fun String.id(id: String) = OpplysningTypeId(id, this)

data class OpplysningTypeId(
    val id: String,
    val beskrivelse: String,
)

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
        parent?.child?.add(this)
    }

    companion object {
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

        fun somBeløp(
            navn: String,
            parent: Opplysningstype<Beløp>? = null,
        ) = somBeløp(navn.id(navn), parent)

        fun somBeløp(
            opplysningTypeId: OpplysningTypeId,
            parent: Opplysningstype<Beløp>? = null,
        ) = Opplysningstype(opplysningTypeId, Penger, parent)

        fun somStønadsperiode(
            navn: String,
            parent: Opplysningstype<Stønadsperiode>? = null,
        ) = somStønadsperiode(navn.id(navn), parent)

        fun somStønadsperiode(
            opplysningTypeId: OpplysningTypeId,
            parent: Opplysningstype<Stønadsperiode>? = null,
        ) = Opplysningstype(opplysningTypeId, Stønadsperiode, parent)

        fun somStønadsdager(
            navn: String,
            parent: Opplysningstype<no.nav.dagpenger.opplysning.verdier.Stønadsdager>? = null,
        ) = somStønadsdager(navn.id(navn), parent)

        fun somStønadsdager(
            opplysningTypeId: OpplysningTypeId,
            parent: Opplysningstype<no.nav.dagpenger.opplysning.verdier.Stønadsdager>? = null,
        ) = Opplysningstype(opplysningTypeId, Stønadsdager, parent)
    }

    override infix fun er(type: Opplysningstype<*>): Boolean = opplysningTypeId == type.opplysningTypeId || parent?.er(type) ?: false

    override fun toString() = "opplysning om $navn"

    override fun equals(other: Any?): Boolean = other is Opplysningstype<*> && other.opplysningTypeId == this.opplysningTypeId

    override fun hashCode() = opplysningTypeId.hashCode() * 31
}
