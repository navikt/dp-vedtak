package no.nav.dagpenger.vedtak.mediator.persistens

interface MeldingRepository {

    fun lagre(melding: Melding)
    fun hentMottatte(): List<Melding>
    fun hentBehandlede(): List<Melding>
    fun behandlet(melding: Melding)
}

interface Melding {
    fun asJson(): String
    fun eier(): String
    fun meldingId(): String
}
