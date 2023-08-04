package no.nav.dagpenger.vedtak.modell.vedtak

import no.nav.dagpenger.vedtak.modell.entitet.Beløp
import no.nav.dagpenger.vedtak.modell.entitet.Stønadsdager
import no.nav.dagpenger.vedtak.modell.utbetaling.Utbetalingsdag
import no.nav.dagpenger.vedtak.modell.utbetaling.Utbetalingsdag.Companion.summer
import no.nav.dagpenger.vedtak.modell.vedtak.Vedtak.VedtakType.Utbetaling
import no.nav.dagpenger.vedtak.modell.visitor.VedtakVisitor
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

class Utbetalingsvedtak(
    vedtakId: UUID = UUID.randomUUID(),
    behandlingId: UUID,
    vedtakstidspunkt: LocalDateTime,
    virkningsdato: LocalDate,
    private val utfall: Boolean,
    private val forbruk: Stønadsdager,
    private val utbetalingsdager: List<Utbetalingsdag>,
    private val trukketEgenandel: Beløp,
) : Vedtak(
    vedtakId = vedtakId,
    behandlingId = behandlingId,
    vedtakstidspunkt = vedtakstidspunkt,
    virkningsdato = virkningsdato,
    type = Utbetaling,
) {

    companion object {
        fun utbetalingsvedtak(
            behandlingId: UUID,
            utfall: Boolean,
            vedtakstidspunkt: LocalDateTime,
            virkningsdato: LocalDate,
            forbruk: Stønadsdager,
            utbetalingsdager: List<Utbetalingsdag>,
            trukketEgenandel: Beløp,
        ) =
            Utbetalingsvedtak(
                behandlingId = behandlingId,
                utfall = utfall,
                vedtakstidspunkt = vedtakstidspunkt,
                virkningsdato = virkningsdato,
                forbruk = forbruk,
                utbetalingsdager = utbetalingsdager,
                trukketEgenandel = trukketEgenandel,
            )
    }
    override fun accept(visitor: VedtakVisitor) {
        visitor.preVisitVedtak(
            vedtakId = vedtakId,
            behandlingId = behandlingId,
            virkningsdato = virkningsdato,
            vedtakstidspunkt = vedtakstidspunkt,
            type = type,
        )

        val beløpTilUtbetaling = utbetalingsdager.summer() - trukketEgenandel
        visitor.visitUtbetalingsvedtak(
            utfall = utfall,
            forbruk = forbruk,
            trukketEgenandel = trukketEgenandel,
            beløpTilUtbetaling = beløpTilUtbetaling,
            utbetalingsdager = utbetalingsdager,
        )

        visitor.postVisitVedtak(
            vedtakId = vedtakId,
            behandlingId = behandlingId,
            virkningsdato = virkningsdato,
            vedtakstidspunkt = vedtakstidspunkt,
            type = type,
        )
    }
}
