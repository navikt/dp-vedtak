package no.nav.dagpenger.avklaring

import no.nav.dagpenger.avklaring.Avklaring.Endring.Avbrutt
import no.nav.dagpenger.avklaring.Avklaring.Endring.Avklart
import no.nav.dagpenger.avklaring.Avklaring.Endring.UnderBehandling
import no.nav.dagpenger.opplysning.UUIDv7
import java.time.LocalDateTime
import java.util.UUID

interface Avklaringkode {
    val name: String
    val tittel: String
    val beskrivelse: String
    val kanKvitteres: Boolean
}

data class Avklaring(
    val id: UUID,
    val kode: Avklaringkode,
    private val historikk: MutableList<Endring> = mutableListOf(UnderBehandling()),
) {
    constructor(kode: Avklaringkode) : this(UUIDv7.ny(), kode)

    private val tilstand get() = historikk.last()

    fun måAvklares() = tilstand is UnderBehandling

    fun erAvklart() = tilstand is Avklart

    fun avbryt() = historikk.add(Avbrutt())

    fun kvittering() {
        require(kode.kanKvitteres) { "Avklaring $kode kan ikke kvitteres ut, krever endring i behandlingen" }
        historikk.add(Avklart("saksbehandler"))
    }

    fun gjenåpne() = historikk.add(UnderBehandling())

    sealed class Endring(
        val endret: LocalDateTime = LocalDateTime.now(),
    ) {
        class UnderBehandling : Endring()

        class Avklart(
            val saksbehandler: String,
        ) : Endring()

        class Avbrutt : Endring()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Avklaring) return false
        return kode == other.kode
    }

    override fun hashCode() = kode.hashCode()
}
