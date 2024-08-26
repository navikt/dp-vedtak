package no.nav.dagpenger.opplysning

import no.nav.dagpenger.opplysning.verdier.Beløp
import no.nav.dagpenger.opplysning.verdier.Inntekt
import no.nav.dagpenger.opplysning.verdier.Ulid
import java.time.LocalDate

interface Klassifiserbart {
    fun er(type: Opplysningstype<*>): Boolean
}

fun String.id(id: String) = OpplysningTypeId(id, this)

fun String.id(
    id: String,
    tekstId: String? = null,
) = OpplysningTypeId(id, this, tekstId)

fun String.tekstId(tekstId: String) = OpplysningTypeId(this, this, tekstId)

data class OpplysningTypeId(
    val id: String,
    val beskrivelse: String,
    val tekstId: String? = null,
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
    val tekstId = opplysningTypeId.tekstId

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

        fun somInntekt(
            navn: String,
            parent: Opplysningstype<Inntekt>? = null,
        ) = somInntekt(navn.id(navn), parent)

        fun somInntekt(
            opplysningTypeId: OpplysningTypeId,
            parent: Opplysningstype<Inntekt>? = null,
        ) = Opplysningstype<Inntekt>(opplysningTypeId, InntektDataType, parent)

        fun somTekst(
            navn: String,
            parent: Opplysningstype<String>? = null,
        ) = somTekst(navn.id(navn), parent)

        fun somTekst(
            opplysningTypeId: OpplysningTypeId,
            parent: Opplysningstype<String>?,
        ) = Opplysningstype(opplysningTypeId, Tekst, parent)
    }

    override infix fun er(type: Opplysningstype<*>): Boolean = opplysningTypeId.id == type.opplysningTypeId.id || parent?.er(type) ?: false

    override fun toString() = "opplysning om $navn"

    override fun equals(other: Any?): Boolean = other is Opplysningstype<*> && other.opplysningTypeId.id == this.opplysningTypeId.id

    override fun hashCode() = opplysningTypeId.id.hashCode() * 31
}
