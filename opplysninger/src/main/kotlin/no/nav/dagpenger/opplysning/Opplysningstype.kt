package no.nav.dagpenger.opplysning

import no.nav.dagpenger.opplysning.verdier.BarnListe
import no.nav.dagpenger.opplysning.verdier.Beløp
import no.nav.dagpenger.opplysning.verdier.Inntekt
import no.nav.dagpenger.opplysning.verdier.Ulid
import java.time.LocalDate
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
    val permanentId: Id<T>,
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

        fun heltall(
            id: Id<Int>,
            beskrivelse: String,
            formål: Opplysningsformål = Opplysningsformål.Regel,
            synlig: (LesbarOpplysninger) -> Boolean = alltidSynlig,
            behovId: String = beskrivelse,
        ): Opplysningstype<Int> = som(id, beskrivelse, formål, synlig, behovId)

        fun boolsk(
            id: Id<Boolean>,
            beskrivelse: String,
            formål: Opplysningsformål = Opplysningsformål.Regel,
            synlig: (LesbarOpplysninger) -> Boolean = alltidSynlig,
            behovId: String = beskrivelse,
        ): Opplysningstype<Boolean> = som(id, beskrivelse, formål, synlig, behovId)

        fun dato(
            id: Id<LocalDate>,
            beskrivelse: String,
            formål: Opplysningsformål = Opplysningsformål.Regel,
            synlig: (LesbarOpplysninger) -> Boolean = alltidSynlig,
            behovId: String = beskrivelse,
        ): Opplysningstype<LocalDate> = som(id, beskrivelse, formål, synlig, behovId)

        fun ulid(
            id: Id<Ulid>,
            beskrivelse: String,
            formål: Opplysningsformål = Opplysningsformål.Regel,
            synlig: (LesbarOpplysninger) -> Boolean = alltidSynlig,
            behovId: String = beskrivelse,
        ): Opplysningstype<Ulid> = som(id, beskrivelse, formål, synlig, behovId)

        fun inntekt(
            id: Id<Inntekt>,
            beskrivelse: String,
            formål: Opplysningsformål = Opplysningsformål.Regel,
            synlig: (LesbarOpplysninger) -> Boolean = alltidSynlig,
            behovId: String = beskrivelse,
        ): Opplysningstype<Inntekt> = som(id, beskrivelse, formål, synlig, behovId)

        fun desimaltall(
            id: Id<Double>,
            beskrivelse: String,
            formål: Opplysningsformål = Opplysningsformål.Regel,
            synlig: (LesbarOpplysninger) -> Boolean = alltidSynlig,
            behovId: String = beskrivelse,
        ): Opplysningstype<Double> = som(id, beskrivelse, formål, synlig, behovId)

        fun tekst(
            id: Id<String>,
            beskrivelse: String,
            formål: Opplysningsformål = Opplysningsformål.Regel,
            synlig: (LesbarOpplysninger) -> Boolean = alltidSynlig,
            behovId: String = beskrivelse,
        ): Opplysningstype<String> = som(id, beskrivelse, formål, synlig, behovId)

        fun beløp(
            id: Id<Beløp>,
            beskrivelse: String,
            formål: Opplysningsformål = Opplysningsformål.Regel,
            synlig: (LesbarOpplysninger) -> Boolean = alltidSynlig,
            behovId: String = beskrivelse,
        ): Opplysningstype<Beløp> = som(id, beskrivelse, formål, synlig, behovId)

        fun barn(
            id: Id<BarnListe>,
            beskrivelse: String,
            formål: Opplysningsformål = Opplysningsformål.Regel,
            synlig: (LesbarOpplysninger) -> Boolean = alltidSynlig,
            behovId: String = beskrivelse,
        ): Opplysningstype<BarnListe> = som(id, beskrivelse, formål, synlig, behovId)

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

    override fun equals(other: Any?): Boolean = other is Opplysningstype<*> && other.permanentId == this.permanentId

    override fun hashCode() = permanentId.hashCode() * 31
}
