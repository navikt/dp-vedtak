package no.nav.dagpenger.opplysning

import no.nav.dagpenger.uuid.UUIDv7
import java.util.UUID

interface Klassifiserbart {
    fun er(type: Opplysningstype<*>): Boolean
}

fun String.id(id: String) = OpplysningTypeId(id = id, beskrivelse = this)

class OpplysningTypeId(
    // todo: Bytte til behovId
    val id: String,
    val beskrivelse: String,
) {
    override fun equals(other: Any?): Boolean = other is OpplysningTypeId && other.id == this.id && other.beskrivelse == this.beskrivelse

    override fun hashCode() = id.hashCode() * beskrivelse.hashCode() * 31
}

enum class Opplysningsformål {
    Legacy(),
    Bruker(),
    Register(),
    Regel(),
}

class Opplysningstype<T : Comparable<T>>(
    val opplysningTypeId: OpplysningTypeId,
    val datatype: Datatype<T>,
    val formål: Opplysningsformål,
    val synlig: (LesbarOpplysninger) -> Boolean = alltidSynlig,
    // todo: Fjerne generering av ny id. MÅ spesifiseres globalt i kodebasen
    val permanentId: Id<T> = Id(UUIDv7.ny(), datatype),
) : Klassifiserbart {
    val id = opplysningTypeId.id
    val navn = opplysningTypeId.beskrivelse

    init {
        definerteTyper.add(this)
    }

    data class Id<T : Comparable<T>>(
        val id: UUID,
        val datatype: Datatype<T>,
    )

    companion object {
        val definerteTyper = mutableSetOf<Opplysningstype<*>>()

        val alltidSynlig: Opplysningssjekk = { true }
        val aldriSynlig: Opplysningssjekk = { false }

        fun somHeltall(
            opplysningTypeId: OpplysningTypeId,
            formål: Opplysningsformål = Opplysningsformål.Regel,
            synlig: (LesbarOpplysninger) -> Boolean = alltidSynlig,
        ) = Opplysningstype(opplysningTypeId, Heltall, formål, synlig)

        fun somHeltall(
            navn: String,
            formål: Opplysningsformål = Opplysningsformål.Regel,
            synlig: (LesbarOpplysninger) -> Boolean = alltidSynlig,
        ) = somHeltall(navn.id(navn), formål, synlig)

        fun somDesimaltall(
            opplysningTypeId: OpplysningTypeId,
            formål: Opplysningsformål = Opplysningsformål.Regel,
            synlig: (LesbarOpplysninger) -> Boolean = alltidSynlig,
        ) = Opplysningstype(opplysningTypeId, Desimaltall, formål, synlig)

        fun somDesimaltall(
            navn: String,
            formål: Opplysningsformål = Opplysningsformål.Regel,
            synlig: (LesbarOpplysninger) -> Boolean = alltidSynlig,
        ) = somDesimaltall(navn.id(navn), formål, synlig)

        fun somDato(
            opplysningTypeId: OpplysningTypeId,
            formål: Opplysningsformål = Opplysningsformål.Regel,
            synlig: (LesbarOpplysninger) -> Boolean = alltidSynlig,
        ) = Opplysningstype(opplysningTypeId, Dato, formål, synlig)

        fun somDato(
            navn: String,
            formål: Opplysningsformål = Opplysningsformål.Regel,
            synlig: (LesbarOpplysninger) -> Boolean = alltidSynlig,
        ) = somDato(navn.id(navn), formål, synlig)

        fun somUlid(
            opplysningTypeId: OpplysningTypeId,
            formål: Opplysningsformål = Opplysningsformål.Regel,
            synlig: (LesbarOpplysninger) -> Boolean = alltidSynlig,
        ) = Opplysningstype(opplysningTypeId, ULID, formål, synlig)

        fun somUlid(
            navn: String,
            formål: Opplysningsformål = Opplysningsformål.Regel,
            synlig: (LesbarOpplysninger) -> Boolean = alltidSynlig,
        ) = somUlid(navn.id(navn), formål, synlig)

        fun somBoolsk(
            opplysningTypeId: OpplysningTypeId,
            formål: Opplysningsformål = Opplysningsformål.Regel,
            synlig: (LesbarOpplysninger) -> Boolean = alltidSynlig,
        ) = Opplysningstype(opplysningTypeId, Boolsk, formål, synlig)

        fun somBoolsk(
            navn: String,
            formål: Opplysningsformål = Opplysningsformål.Regel,
            synlig: (LesbarOpplysninger) -> Boolean = alltidSynlig,
        ) = somBoolsk(navn.id(navn), formål, synlig)

        fun somBeløp(
            navn: String,
            formål: Opplysningsformål = Opplysningsformål.Regel,
            synlig: (LesbarOpplysninger) -> Boolean = alltidSynlig,
        ) = somBeløp(navn.id(navn), formål, synlig)

        fun somBeløp(
            opplysningTypeId: OpplysningTypeId,
            formål: Opplysningsformål = Opplysningsformål.Regel,
            synlig: (LesbarOpplysninger) -> Boolean = alltidSynlig,
        ) = Opplysningstype(opplysningTypeId, Penger, formål, synlig)

        fun somInntekt(
            navn: String,
            formål: Opplysningsformål = Opplysningsformål.Regel,
            synlig: (LesbarOpplysninger) -> Boolean = alltidSynlig,
        ) = somInntekt(navn.id(navn), formål, synlig)

        fun somInntekt(
            opplysningTypeId: OpplysningTypeId,
            formål: Opplysningsformål = Opplysningsformål.Regel,
            synlig: (LesbarOpplysninger) -> Boolean = alltidSynlig,
        ) = Opplysningstype(opplysningTypeId, InntektDataType, formål, synlig)

        fun somBarn(
            navn: String,
            formål: Opplysningsformål = Opplysningsformål.Regel,
            synlig: (LesbarOpplysninger) -> Boolean = alltidSynlig,
        ) = somBarn(navn.id(navn), formål, synlig)

        fun somBarn(
            opplysningTypeId: OpplysningTypeId,
            formål: Opplysningsformål = Opplysningsformål.Regel,
            synlig: (LesbarOpplysninger) -> Boolean = alltidSynlig,
        ) = Opplysningstype(opplysningTypeId, BarnDatatype, formål, synlig)

        fun somTekst(
            navn: String,
            formål: Opplysningsformål = Opplysningsformål.Regel,
            synlig: (LesbarOpplysninger) -> Boolean = alltidSynlig,
        ) = somTekst(navn.id(navn), formål, synlig)

        fun somTekst(
            opplysningTypeId: OpplysningTypeId,
            formål: Opplysningsformål = Opplysningsformål.Regel,
            synlig: (LesbarOpplysninger) -> Boolean = alltidSynlig,
        ) = Opplysningstype(opplysningTypeId, Tekst, formål, synlig)

        fun <T : Comparable<T>> som(
            id: Id<T>,
            beskrivelse: String,
            formål: Opplysningsformål = Opplysningsformål.Regel,
            synlig: (LesbarOpplysninger) -> Boolean = alltidSynlig,
            behovId: String = beskrivelse,
        ): Opplysningstype<T> = Opplysningstype(OpplysningTypeId(behovId, beskrivelse), id.datatype, formål, synlig, id)
    }

    override infix fun er(type: Opplysningstype<*>): Boolean = opplysningTypeId == type.opplysningTypeId

    override fun toString() = navn

    override fun equals(other: Any?): Boolean = other is Opplysningstype<*> && other.opplysningTypeId == this.opplysningTypeId

    override fun hashCode() = opplysningTypeId.hashCode() * 31
}
