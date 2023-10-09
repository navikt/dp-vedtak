package no.nav.dagpenger.vedtak.modell.vedtak

import no.nav.dagpenger.vedtak.modell.SakId
import no.nav.dagpenger.vedtak.modell.entitet.Periode
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

interface VedtakObserver {

    fun vedtakFattet(vedtakFattet: VedtakFattet) {}
    fun utbetalingsvedtakFattet(utbetalingsvedtakFattet: UtbetalingsvedtakFattet) {}

    enum class Utfall {
        Innvilget,
        Avslått,
    }

    data class VedtakFattet(
        val vedtakId: UUID,
        val sakId: SakId,
        val behandlingId: UUID,
        val vedtakstidspunkt: LocalDateTime,
        val virkningsdato: LocalDate,
        val utfall: Utfall,
        // @todo: Type rettighet? Ordinær, Permittering etc
    )

    data class UtbetalingsvedtakFattet(
        val vedtakId: UUID,
        val sakId: SakId,
        val behandlingId: UUID,
        val periode: Periode,
        val vedtakstidspunkt: LocalDateTime,
        val virkningsdato: LocalDate,
        val utfall: Utfall,
        val utbetalingsdager: List<UtbetalingsdagDto> = emptyList(),
        // @todo: Type rettighet? Ordinær, Permittering etc
    )

    data class UtbetalingsdagDto(val dato: LocalDate, val beløp: Double) // TODO: Avventer avrundsregler: https://favro.com/organization/98c34fb974ce445eac854de0/696529a0ddfa866861cfa6b6?card=NAV-13898
}
