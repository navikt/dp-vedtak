package no.nav.dagpenger.vedtak.mediator.persistens

import no.nav.dagpenger.vedtak.mediator.melding.HendelseMessage
import no.nav.dagpenger.vedtak.mediator.melding.HendelseRepository
import java.util.UUID

internal class InMemoryMeldingRepository : HendelseRepository {

    private val meldingDb = mutableMapOf<UUID, MeldingDto>()

    override fun hentMottatte(): List<HendelseMessage> = hentMeldingerMedStatus(MeldingStatus.MOTTATT)
    override fun hentBehandlede(): List<HendelseMessage> = hentMeldingerMedStatus(MeldingStatus.BEHANDLET)
    override fun lagreMelding(hendelseMessage: HendelseMessage, ident: String, id: UUID, toJson: String) {
        meldingDb[id] = MeldingDto(hendelseMessage, MeldingStatus.MOTTATT)
    }

    override fun markerSomBehandlet(meldingId: UUID) {
        val melding = hentMelding(meldingId)
        melding.status = MeldingStatus.BEHANDLET
        meldingDb[meldingId] = melding
    }

    private fun hentMeldingerMedStatus(status: MeldingStatus) =
        meldingDb.values.filter { it.status == status }.map { it.hendelseMessage }

    private fun hentMelding(id: UUID) = (
        meldingDb[id]
            ?: throw IllegalArgumentException("Melding med id $id finnes ikke")
        )

    private data class MeldingDto(val hendelseMessage: HendelseMessage, var status: MeldingStatus)

    private enum class MeldingStatus {
        MOTTATT, BEHANDLET
    }
}
