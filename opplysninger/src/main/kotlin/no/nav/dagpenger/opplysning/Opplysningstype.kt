package no.nav.dagpenger.opplysning

interface Klassifiserbart {
    fun er(type: Opplysningstype<*>): Boolean
}

fun String.id(id: String) = OpplysningTypeId(id, this)

fun String.id(
    id: String,
    tekstId: String? = null,
) = OpplysningTypeId(id, this, tekstId)

fun String.tekstId(tekstId: String) = OpplysningTypeId(this, this, tekstId)

class OpplysningTypeId(
    val id: String,
    val beskrivelse: String,
    val tekstId: String? = null,
) {
    override fun equals(other: Any?): Boolean = other is OpplysningTypeId && other.id == this.id && other.beskrivelse == this.beskrivelse

    override fun hashCode() = id.hashCode() * beskrivelse.hashCode() * 31
}

class Opplysningstype<T : Comparable<T>>(
    private val opplysningTypeId: OpplysningTypeId,
    val datatype: Datatype<T>,
) : Klassifiserbart {
    constructor(
        navn: String,
        datatype: Datatype<T>,
    ) : this(OpplysningTypeId(navn, navn), datatype)

    val id = opplysningTypeId.id
    val navn = opplysningTypeId.beskrivelse
    val tekstId = opplysningTypeId.tekstId

    companion object {
        fun somHeltall(opplysningTypeId: OpplysningTypeId) = Opplysningstype(opplysningTypeId, Heltall)

        fun somHeltall(navn: String) = somHeltall(navn.id(navn))

        fun somDesimaltall(opplysningTypeId: OpplysningTypeId) = Opplysningstype(opplysningTypeId, Desimaltall)

        fun somDesimaltall(navn: String) = somDesimaltall(navn.id(navn))

        fun somDato(opplysningTypeId: OpplysningTypeId) = Opplysningstype(opplysningTypeId, Dato)

        fun somDato(navn: String) = somDato(navn.id(navn))

        fun somUlid(opplysningTypeId: OpplysningTypeId) = Opplysningstype(opplysningTypeId, ULID)

        fun somUlid(navn: String) = somUlid(navn.id(navn))

        fun somBoolsk(opplysningTypeId: OpplysningTypeId) = Opplysningstype(opplysningTypeId, Boolsk)

        fun somBoolsk(navn: String) = somBoolsk(navn.id(navn))

        fun somBeløp(navn: String) = somBeløp(navn.id(navn))

        fun somBeløp(opplysningTypeId: OpplysningTypeId) = Opplysningstype(opplysningTypeId, Penger)

        fun somInntekt(navn: String) = somInntekt(navn.id(navn))

        fun somInntekt(opplysningTypeId: OpplysningTypeId) = Opplysningstype(opplysningTypeId, InntektDataType)

        fun somBarn(navn: String) = somBarn(navn.id(navn))

        fun somBarn(opplysningTypeId: OpplysningTypeId) = Opplysningstype(opplysningTypeId, BarnDatatype)

        fun somTekst(navn: String) = somTekst(navn.id(navn))

        fun somTekst(opplysningTypeId: OpplysningTypeId) = Opplysningstype(opplysningTypeId, Tekst)
    }

    override infix fun er(type: Opplysningstype<*>): Boolean = opplysningTypeId == type.opplysningTypeId

    override fun toString() = navn

    override fun equals(other: Any?): Boolean = other is Opplysningstype<*> && other.opplysningTypeId == this.opplysningTypeId

    override fun hashCode() = opplysningTypeId.hashCode() * 31
}
