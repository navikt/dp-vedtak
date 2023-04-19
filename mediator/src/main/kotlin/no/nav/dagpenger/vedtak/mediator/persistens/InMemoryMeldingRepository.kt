package no.nav.dagpenger.vedtak.mediator.persistens

class InMemoryMeldingRepository : MeldingRepository {

    private val meldingDb = mutableMapOf<String, Melding>()

    override fun lagre(melding: Melding) {
        meldingDb[melding.eier()] = melding
    }

    override fun hent(): List<Melding> = meldingDb.values.toList()
}
