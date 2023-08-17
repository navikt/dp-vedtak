package no.nav.dagpenger.vedtak.modell.vedtak

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
    sakId: String,
    behandlingId: UUID,
    vedtakstidspunkt: LocalDateTime,
    virkningsdato: LocalDate,
    private val utfall: Boolean,
    private val forbruk: Stønadsdager,
    private val utbetalingsdager: List<Utbetalingsdag>,
    private val forrigeBehandlingId: UUID?,
) : Vedtak(
    vedtakId = vedtakId,
    sakId = sakId,
    behandlingId = behandlingId,
    vedtakstidspunkt = vedtakstidspunkt,
    virkningsdato = virkningsdato,
    type = Utbetaling,
) {

    companion object {
        fun utbetalingsvedtak(
            behandlingId: UUID,
            sakId: String,
            utfall: Boolean,
            vedtakstidspunkt: LocalDateTime,
            virkningsdato: LocalDate,
            forrigeBehandlingId: UUID?,
            forbruk: Stønadsdager,
            utbetalingsdager: List<Utbetalingsdag>,
        ) =
            Utbetalingsvedtak(
                sakId = sakId,
                behandlingId = behandlingId,
                utfall = utfall,
                vedtakstidspunkt = vedtakstidspunkt,
                virkningsdato = virkningsdato,
                forrigeBehandlingId = forrigeBehandlingId,
                forbruk = forbruk,
                utbetalingsdager = utbetalingsdager,
            )
    }
    override fun accept(visitor: VedtakVisitor) {
        visitor.preVisitVedtak(
            vedtakId = vedtakId,
            sakId = sakId,
            behandlingId = behandlingId,
            virkningsdato = virkningsdato,
            vedtakstidspunkt = vedtakstidspunkt,
            type = type,
        )

        visitor.visitUtbetalingsvedtak(
            utfall = utfall,
            forbruk = forbruk,
            beløpTilUtbetaling = utbetalingsdager.summer(),
            utbetalingsdager = utbetalingsdager,
            forrigeBehandlingId = null,
        )

        visitor.postVisitVedtak(
            vedtakId = vedtakId,
            sakId = sakId,
            behandlingId = behandlingId,
            virkningsdato = virkningsdato,
            vedtakstidspunkt = vedtakstidspunkt,
            type = type,
        )
    }
}
