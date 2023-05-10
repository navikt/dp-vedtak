package no.nav.dagpenger.vedtak.iverksetting.mediator

import no.nav.dagpenger.vedtak.iverksetting.Iverksetting
import no.nav.dagpenger.vedtak.iverksetting.mediator.persistens.IverksettingRepository
import java.util.UUID

class InMemoryIverksettingRepository : IverksettingRepository {
    private val iverksettingDb = mutableMapOf<UUID, Iverksetting>()

    override fun hent(vedtakId: UUID): Iverksetting? = iverksettingDb[vedtakId]

    override fun lagre(iverksetting: Iverksetting) {
        iverksettingDb[iverksetting.id] = iverksetting
    }
}
