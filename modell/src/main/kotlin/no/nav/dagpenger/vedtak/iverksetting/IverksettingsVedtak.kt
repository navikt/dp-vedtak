package no.nav.dagpenger.vedtak.iverksetting

import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

data class IverksettingsVedtak(
    val vedtakId: UUID,
    val behandlingId: UUID,
    val vedtakstidspunkt: LocalDateTime,
    val virkningsdato: LocalDate,
    val utbetalingsdager: List<Utbetalingsdag>,
    val utfall: Utfall,
) {
    enum class Utfall {
        Innvilget,
        Avslått,
    }

    data class Utbetalingsdag(val dato: LocalDate, val beløp: Double)
}
