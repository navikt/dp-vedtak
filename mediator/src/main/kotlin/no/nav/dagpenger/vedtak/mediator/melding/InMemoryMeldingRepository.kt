package no.nav.dagpenger.vedtak.mediator.melding

import java.util.UUID


// @todo: Imidlertidig løsning før vi får på plass en ordentlig databasetabell
internal class InMemoryMeldingRepository : HendelseRepository {
    private val meldingDb = mutableMapOf<UUID, MeldingDto>()

    override fun lagreMelding(
        hendelseMessage: HendelseMessage,
        ident: String,
        id: UUID,
        toJson: String,
    ) {
        meldingDb[id] = MeldingDto(hendelseMessage, MeldingStatus.MOTTATT)
    }

    override fun markerSomBehandlet(hendelseId: UUID): Int {
        val melding = hentMelding(hendelseId)
        melding.status = MeldingStatus.BEHANDLET
        meldingDb[hendelseId] = melding
        return 1
    }

    override fun erBehandlet(hendelseId: UUID): Boolean {
        return meldingDb[hendelseId]?.status == MeldingStatus.BEHANDLET
    }

    private fun hentMelding(id: UUID) =
        (
                meldingDb[id]
                    ?: throw IllegalArgumentException("Melding med id $id finnes ikke")
                )

    private data class MeldingDto(val hendelseMessage: HendelseMessage, var status: MeldingStatus)

    private enum class MeldingStatus {
        MOTTATT,
        BEHANDLET,
    }
}
