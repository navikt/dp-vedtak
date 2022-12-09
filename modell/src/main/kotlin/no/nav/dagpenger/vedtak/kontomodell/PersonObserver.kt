package no.nav.dagpenger.vedtak.kontomodell

import no.nav.dagpenger.vedtak.kontomodell.mengder.Penger
import java.util.UUID

interface PersonObserver {
    data class VedtakFattetEvent(val vedtakId: UUID, val avtaleId: UUID, val sats: Penger)
    data class VedtakEndretEvent(val vedtakId: String, val avtaleId: UUID)

    fun vedtakFattet(hendelse: VedtakFattetEvent) {}

    fun vedtakEndret(hendelse: VedtakEndretEvent) {}
}
