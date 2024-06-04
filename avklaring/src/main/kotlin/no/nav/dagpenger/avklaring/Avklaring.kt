package no.nav.dagpenger.avklaring

import java.time.LocalDateTime

interface Avklaringkode {
    val kode: String
    val tittel: String
    val beskrivelse: String
}

data class Avklaring(
    val kode: Avklaringkode,
    private val historikk: MutableList<Endring> = mutableListOf(Endring.Opprettet()),
) {
    val tilstand =
        when (historikk.last()) {
            is Endring.Opprettet -> Avklaringtilstand.Opprettet
            is Endring.UnderBehandling -> Avklaringtilstand.UnderBehandling
            is Endring.Avklart -> Avklaringtilstand.Avklart
            is Endring.Avbrutt -> Avklaringtilstand.Avbrutt
        }

    fun erAvklart() = tilstand == Avklaringtilstand.Avklart

    fun måAvklares() = tilstand != Avklaringtilstand.Avklart && tilstand != Avklaringtilstand.Avbrutt

    enum class Avklaringtilstand {
        Opprettet,
        UnderBehandling,
        Avklart,
        Avbrutt,
    }

    sealed class Endring(val endret: LocalDateTime = LocalDateTime.now()) {
        class Opprettet : Endring()

        class UnderBehandling : Endring()

        class Avklart(val saksbehandler: String) : Endring()

        class Avbrutt : Endring()
    }
}
