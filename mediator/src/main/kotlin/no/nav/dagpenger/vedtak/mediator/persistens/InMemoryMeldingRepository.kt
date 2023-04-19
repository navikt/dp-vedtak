package no.nav.dagpenger.vedtak.mediator.persistens

class InMemoryMeldingRepository : MeldingRepository {

    private val meldingDb = mutableMapOf<String, MeldingDto>()

    override fun lagre(melding: Melding) {
        meldingDb[melding.meldingId()] = MeldingDto(melding, MeldingStatus.MOTTATT)
    }

    override fun hentMottatte(): List<Melding> = meldingDb.values.filter { it.status == MeldingStatus.MOTTATT }.map { it.melding }
    override fun hentBehandlede(): List<Melding> = meldingDb.values.filter { it.status == MeldingStatus.BEHANDLET }.map { it.melding }
    override fun behandlet(melding: Melding) {
        val meldingDto = meldingDb.get(melding.meldingId())
            ?: throw IllegalArgumentException("Melding med id ${melding.meldingId()} finnes ikke")
        meldingDto.status = MeldingStatus.BEHANDLET
        meldingDb[melding.meldingId()] = meldingDto
    }

    private data class MeldingDto(val melding: Melding, var status: MeldingStatus)

    private enum class MeldingStatus {
        MOTTATT, BEHANDLET, FEILET
    }
}
