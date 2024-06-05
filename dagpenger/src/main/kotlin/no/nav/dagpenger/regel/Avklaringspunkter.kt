package no.nav.dagpenger.regel

import no.nav.dagpenger.avklaring.Avklaringkode

enum class Avklaringspunkter(
    override val kode: String,
    override val tittel: String,
    override val beskrivelse: String,
    override val kanKvitteres: Boolean = true,
) : Avklaringkode {
    Verneplikt("Verneplikt", "Verneplikt", "Krever avklaring om verneplikt"),
}
