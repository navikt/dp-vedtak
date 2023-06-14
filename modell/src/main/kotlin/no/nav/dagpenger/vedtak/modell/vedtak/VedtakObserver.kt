package no.nav.dagpenger.vedtak.modell.vedtak

import no.nav.dagpenger.vedtak.modell.utbetaling.LøpendeRettighetDag
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

interface VedtakObserver {

    fun rammevedtakFattet(rammevedtakFattet: RammevedtakFattet) {}
    fun løpendeVedtakFattet(løpendeVedtakFattet: LøpendeVedtakFattet) {}

    enum class Utfall {
        Innvilget,
        Avslått,
    }

    data class RammevedtakFattet(
        val vedtakId: UUID,
        val behandlingId: UUID,
        val vedtakstidspunkt: LocalDateTime,
        val virkningsdato: LocalDate,
        val utfall: Utfall,
        // @todo: Type rettighet? Ordinær, Permittering etc
    )

    data class LøpendeVedtakFattet(
        val vedtakId: UUID,
        val behandlingId: UUID,
        val vedtakstidspunkt: LocalDateTime,
        val virkningsdato: LocalDate,
        val utbetalingsdager: List<LøpendeRettighetDag> = emptyList(),
        val utfall: Utfall,
        // @todo: Type rettighet? Ordinær, Permittering etc
    )
}
