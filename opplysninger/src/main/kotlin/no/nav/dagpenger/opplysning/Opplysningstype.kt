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

enum class Opplysningsformål(
    val synlig: Boolean,
) {
    Legacy(false),
    Mellomsteg(false),
    Bruker(true),
    Register(true),
    Regel(true),
}

class Opplysningstype<T : Comparable<T>>(
    private val opplysningTypeId: OpplysningTypeId,
    val datatype: Datatype<T>,
    val formål: Opplysningsformål,
) : Klassifiserbart {
    val id = opplysningTypeId.id
    val navn = opplysningTypeId.beskrivelse
    val tekstId = opplysningTypeId.tekstId

    init {
        definerteTyper.add(this)
    }

    companion object {
        val definerteTyper = mutableSetOf<Opplysningstype<*>>()

        fun somHeltall(
            opplysningTypeId: OpplysningTypeId,
            formål: Opplysningsformål = Opplysningsformål.Regel,
        ) = Opplysningstype(opplysningTypeId, Heltall, formål)

        fun somHeltall(
            navn: String,
            formål: Opplysningsformål = Opplysningsformål.Regel,
        ) = somHeltall(navn.id(navn), formål)

        fun somDesimaltall(
            opplysningTypeId: OpplysningTypeId,
            formål: Opplysningsformål = Opplysningsformål.Regel,
        ) = Opplysningstype(opplysningTypeId, Desimaltall, formål)

        fun somDesimaltall(
            navn: String,
            formål: Opplysningsformål = Opplysningsformål.Regel,
        ) = somDesimaltall(navn.id(navn), formål)

        fun somDato(
            opplysningTypeId: OpplysningTypeId,
            formål: Opplysningsformål = Opplysningsformål.Regel,
        ) = Opplysningstype(opplysningTypeId, Dato, formål)

        fun somDato(
            navn: String,
            formål: Opplysningsformål = Opplysningsformål.Regel,
        ) = somDato(navn.id(navn), formål)

        fun somUlid(
            opplysningTypeId: OpplysningTypeId,
            formål: Opplysningsformål = Opplysningsformål.Regel,
        ) = Opplysningstype(opplysningTypeId, ULID, formål)

        fun somUlid(
            navn: String,
            formål: Opplysningsformål = Opplysningsformål.Regel,
        ) = somUlid(navn.id(navn), formål)

        fun somBoolsk(
            opplysningTypeId: OpplysningTypeId,
            formål: Opplysningsformål = Opplysningsformål.Regel,
        ) = Opplysningstype(opplysningTypeId, Boolsk, formål)

        fun somBoolsk(
            navn: String,
            formål: Opplysningsformål = Opplysningsformål.Regel,
        ) = somBoolsk(navn.id(navn), formål)

        fun somBeløp(
            navn: String,
            formål: Opplysningsformål = Opplysningsformål.Regel,
        ) = somBeløp(navn.id(navn), formål)

        fun somBeløp(
            opplysningTypeId: OpplysningTypeId,
            formål: Opplysningsformål = Opplysningsformål.Regel,
        ) = Opplysningstype(opplysningTypeId, Penger, formål)

        fun somInntekt(
            navn: String,
            formål: Opplysningsformål = Opplysningsformål.Regel,
        ) = somInntekt(navn.id(navn), formål)

        fun somInntekt(
            opplysningTypeId: OpplysningTypeId,
            formål: Opplysningsformål = Opplysningsformål.Regel,
        ) = Opplysningstype(opplysningTypeId, InntektDataType, formål)

        fun somBarn(
            navn: String,
            formål: Opplysningsformål = Opplysningsformål.Regel,
        ) = somBarn(navn.id(navn), formål)

        fun somBarn(
            opplysningTypeId: OpplysningTypeId,
            formål: Opplysningsformål = Opplysningsformål.Regel,
        ) = Opplysningstype(opplysningTypeId, BarnDatatype, formål)

        fun somTekst(
            navn: String,
            formål: Opplysningsformål = Opplysningsformål.Regel,
        ) = somTekst(navn.id(navn), formål)

        fun somTekst(
            opplysningTypeId: OpplysningTypeId,
            formål: Opplysningsformål = Opplysningsformål.Regel,
        ) = Opplysningstype(opplysningTypeId, Tekst, formål)
    }

    override infix fun er(type: Opplysningstype<*>): Boolean = opplysningTypeId == type.opplysningTypeId

    override fun toString() = navn

    override fun equals(other: Any?): Boolean = other is Opplysningstype<*> && other.opplysningTypeId == this.opplysningTypeId

    override fun hashCode() = opplysningTypeId.hashCode() * 31
}
