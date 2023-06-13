package no.nav.dagpenger.vedtak.modell.vedtak

import no.nav.dagpenger.vedtak.modell.utbetaling.LøpendeRettighetDag
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

interface VedtakObserver {

    fun vedtakFattet(vedtakFattet: VedtakFattet) {}

    data class VedtakFattet(
        val vedtakId: UUID,
        val behandlingId: UUID,
        val vedtakstidspunkt: LocalDateTime,
        val virkningsdato: LocalDate,
        val utbetalingsdager: List<LøpendeRettighetDag> = emptyList(),
        val utfall: Utfall,
        // @todo: Type rettighet? Ordinær, Permittering etc

    ) {
        enum class Utfall {
            Innvilget,
            Avslått,
        }
    }
}
