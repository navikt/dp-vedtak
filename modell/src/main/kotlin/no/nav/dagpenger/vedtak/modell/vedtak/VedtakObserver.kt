package no.nav.dagpenger.vedtak.modell.vedtak

import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

interface VedtakObserver {

    fun vedtakFattet(vedtakFattet: VedtakFattet) {}
    fun løpendeVedtakFattet(utbetalingVedtakFattet: UtbetalingVedtakFattet) {}

    enum class Utfall {
        Innvilget,
        Avslått,
    }

    data class VedtakFattet(
        val vedtakId: UUID,
        val behandlingId: UUID,
        val vedtakstidspunkt: LocalDateTime,
        val virkningsdato: LocalDate,
        val utfall: Utfall,
        // @todo: Type rettighet? Ordinær, Permittering etc
    )

    data class UtbetalingVedtakFattet(
        val vedtakId: UUID,
        val behandlingId: UUID,
        val vedtakstidspunkt: LocalDateTime,
        val virkningsdato: LocalDate,
        val utbetalingsdager: List<UtbetalingsdagDto> = emptyList(),
        val utfall: Utfall,
        // @todo: Type rettighet? Ordinær, Permittering etc
    )

    data class UtbetalingsdagDto(val dato: LocalDate, val beløp: Double) // TODO: Avventer avrundsregler: https://favro.com/organization/98c34fb974ce445eac854de0/696529a0ddfa866861cfa6b6?card=NAV-13898
}
