package no.nav.dagpenger.vedtak.mediator.persistens

class InMemoryMeldingRepository : MeldingRepository {

    private val meldingDb = mutableMapOf<String, MeldingDto>()

    override fun lagre(hendelseMessage: HendelseMessage) {
        meldingDb[hendelseMessage.meldingId()] = MeldingDto(hendelseMessage, MeldingStatus.MOTTATT)
    }

    override fun hentMottatte(): List<HendelseMessage> = hentMeldingerMedStatus(MeldingStatus.MOTTATT)
    override fun hentBehandlede(): List<HendelseMessage> = hentMeldingerMedStatus(MeldingStatus.BEHANDLET)
    override fun hentFeilede(): List<HendelseMessage> = hentMeldingerMedStatus(MeldingStatus.FEILET)

    override fun behandlet(hendelseMessage: HendelseMessage) {
        val meldingDto = hentMelding(hendelseMessage)
        meldingDto.status = MeldingStatus.BEHANDLET
        meldingDb[hendelseMessage.meldingId()] = meldingDto
    }

    fun feilet(hendelseMessage: HendelseMessage) {
        val meldingDto = hentMelding(hendelseMessage)
        meldingDto.status = MeldingStatus.FEILET
        meldingDb[hendelseMessage.meldingId()] = meldingDto
    }

    private fun hentMeldingerMedStatus(status: MeldingStatus) =
        meldingDb.values.filter { it.status == status }.map { it.hendelseMessage }

    private fun hentMelding(hendelseMessage: HendelseMessage) = (
        meldingDb[hendelseMessage.meldingId()]
            ?: throw IllegalArgumentException("Melding med id ${hendelseMessage.meldingId()} finnes ikke")
        )

    private data class MeldingDto(val hendelseMessage: HendelseMessage, var status: MeldingStatus)

    private enum class MeldingStatus {
        MOTTATT, BEHANDLET, FEILET
    }
}
