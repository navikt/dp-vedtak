package no.nav.dagpenger.vedtak.iverksetting.hendelser

import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

class UtbetalingsvedtakFattetHendelse(
    meldingsreferanseId: UUID,
    ident: String,
    vedtakId: UUID,
    behandlingId: UUID,
    sakId: String,
    val vedtakstidspunkt: LocalDateTime,
    val virkningsdato: LocalDate,
    val utbetalingsdager: List<Utbetalingsdag>,
    val utfall: Utfall,
) : VedtakFattetHendelse(meldingsreferanseId, ident, vedtakId, behandlingId, sakId) {
    data class Utbetalingsdag(val dato: LocalDate, val beløp: Double)
    enum class Utfall {
        Innvilget,
        Avslått,
    }
}
