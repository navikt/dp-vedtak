package no.nav.dagpenger.avklaring

import no.nav.dagpenger.avklaring.Avklaring.Endring.Avbrutt
import no.nav.dagpenger.avklaring.Avklaring.Endring.Avklart
import no.nav.dagpenger.avklaring.Avklaring.Endring.UnderBehandling
import no.nav.dagpenger.opplysning.UUIDv7
import java.time.LocalDateTime
import java.util.UUID

data class Avklaringkode(
    val kode: String,
    val tittel: String,
    val beskrivelse: String,
    val kanKvitteres: Boolean = true,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Avklaringkode) return false
        return kode == other.kode
    }

    override fun hashCode() = kode.hashCode()
}

data class Avklaring(
    val id: UUID,
    val kode: Avklaringkode,
    private val historikk: MutableList<Endring> = mutableListOf(UnderBehandling()),
) {
    constructor(kode: Avklaringkode) : this(UUIDv7.ny(), kode)

    private val tilstand get() = historikk.last()

    val endringer get() = historikk.toList()

    fun måAvklares() = tilstand is UnderBehandling

    fun erAvklart() = tilstand is Avklart

    fun avbryt() = historikk.add(Avbrutt())

    fun kvittering() {
        require(kode.kanKvitteres) { "Avklaring $kode kan ikke kvitteres ut, krever endring i behandlingen" }
        historikk.add(Avklart("saksbehandler"))
    }

    fun gjenåpne() = historikk.add(UnderBehandling())

    sealed class Endring(
        val id: UUID,
        open val endret: LocalDateTime,
    ) : Comparable<Endring> {
        override fun compareTo(other: Endring) = endret.compareTo(other.endret)

        data class UnderBehandling(
            override val endret: LocalDateTime = LocalDateTime.now(),
        ) : Endring(UUIDv7.ny(), endret)

        data class Avklart(
            val saksbehandler: String,
            override val endret: LocalDateTime = LocalDateTime.now(),
        ) : Endring(UUIDv7.ny(), endret)

        data class Avbrutt(
            override val endret: LocalDateTime = LocalDateTime.now(),
        ) : Endring(UUIDv7.ny(), endret)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Avklaring) return false
        return kode == other.kode
    }

    override fun hashCode() = kode.hashCode()
}
