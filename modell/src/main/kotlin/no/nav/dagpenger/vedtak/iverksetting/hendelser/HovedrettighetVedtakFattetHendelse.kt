package no.nav.dagpenger.vedtak.iverksetting.hendelser

import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

class HovedrettighetVedtakFattetHendelse(
    meldingsreferanseId: UUID,
    ident: String,
    vedtakId: UUID,
    behandlingId: UUID,
    val vedtakstidspunkt: LocalDateTime,
    val virkningsdato: LocalDate,
    val utfall: Utfall,
) : VedtakFattetHendelse(meldingsreferanseId, ident, vedtakId, behandlingId) {
    override fun kontekstMap(): Map<String, String> = mapOf("vedtakId" to vedtakId.toString(), "behandlingId" to behandlingId.toString())

    enum class Utfall {
        Innvilget,
        Avslått,
    }
}
