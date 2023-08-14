package no.nav.dagpenger.vedtak.iverksetting.hendelser

import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

class DagpengerInnvilget(
    meldingsreferanseId: UUID,
    ident: String,
    vedtakId: UUID,
    behandlingId: UUID,
    sakId: String,
    val vedtakstidspunkt: LocalDateTime,
    val virkningsdato: LocalDate,
) : VedtakFattetHendelse(meldingsreferanseId, ident, vedtakId, behandlingId, sakId) {
    override fun kontekstMap(): Map<String, String> = mapOf("vedtakId" to vedtakId.toString(), "behandlingId" to behandlingId.toString())
}
