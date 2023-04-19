package no.nav.dagpenger.vedtak.mediator.persistens

interface MeldingRepository {

    fun lagre(melding: Melding)
    fun hent(): List<Melding>
}

interface Melding {
    fun asJson(): String
    fun eier(): String
}
