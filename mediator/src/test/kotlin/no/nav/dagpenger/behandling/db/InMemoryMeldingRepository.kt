package no.nav.dagpenger.behandling.db

import no.nav.dagpenger.behandling.mediator.melding.HendelseMessage
import no.nav.dagpenger.behandling.mediator.melding.HendelseRepository
import java.util.UUID

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

    override fun markerSomBehandlet(meldingId: UUID): Int {
        val melding = hentMelding(meldingId)
        melding.status = MeldingStatus.BEHANDLET
        meldingDb[meldingId] = melding
        return 1
    }

    override fun erBehandlet(meldingId: UUID): Boolean {
        return meldingDb[meldingId]?.status == MeldingStatus.BEHANDLET
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
