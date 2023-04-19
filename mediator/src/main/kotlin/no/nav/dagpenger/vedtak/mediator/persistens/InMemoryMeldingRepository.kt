package no.nav.dagpenger.vedtak.mediator.persistens

class InMemoryMeldingRepository : MeldingRepository {

    private val meldingDb = mutableMapOf<String, MeldingDto>()

    override fun lagre(melding: Melding) {
        meldingDb[melding.meldingId()] = MeldingDto(melding, MeldingStatus.MOTTATT)
    }

    override fun hentMottatte(): List<Melding> = hentMeldingerMedStatus(MeldingStatus.MOTTATT)
    override fun hentBehandlede(): List<Melding> = hentMeldingerMedStatus(MeldingStatus.BEHANDLET)
    override fun hentFeilede(): List<Melding> = hentMeldingerMedStatus(MeldingStatus.FEILET)

    override fun behandlet(melding: Melding) {
        val meldingDto = hentMelding(melding)
        meldingDto.status = MeldingStatus.BEHANDLET
        meldingDb[melding.meldingId()] = meldingDto
    }

    fun feilet(melding: Melding) {
        val meldingDto = hentMelding(melding)
        meldingDto.status = MeldingStatus.FEILET
        meldingDb[melding.meldingId()] = meldingDto
    }

    private fun hentMeldingerMedStatus(status: MeldingStatus) =
        meldingDb.values.filter { it.status == status }.map { it.melding }

    private fun hentMelding(melding: Melding) = (
        meldingDb[melding.meldingId()]
            ?: throw IllegalArgumentException("Melding med id ${melding.meldingId()} finnes ikke")
        )

    private data class MeldingDto(val melding: Melding, var status: MeldingStatus)

    private enum class MeldingStatus {
        MOTTATT, BEHANDLET, FEILET
    }
}
