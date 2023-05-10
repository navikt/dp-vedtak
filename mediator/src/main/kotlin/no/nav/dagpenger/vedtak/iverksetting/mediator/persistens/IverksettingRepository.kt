package no.nav.dagpenger.vedtak.iverksetting.mediator.persistens

import no.nav.dagpenger.vedtak.iverksetting.Iverksetting
import java.util.UUID

interface IverksettingRepository {
    fun hent(vedtakId: UUID): Iverksetting?
    fun lagre(iverksetting: Iverksetting)
}
