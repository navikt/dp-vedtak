package no.nav.dagpenger.vedtak.modell.vedtak

import no.nav.dagpenger.vedtak.modell.entitet.Beløp
import no.nav.dagpenger.vedtak.modell.entitet.Beløp.Companion.beløp
import no.nav.dagpenger.vedtak.modell.entitet.Stønadsdager
import no.nav.dagpenger.vedtak.modell.utbetaling.Utbetalingsdag
import no.nav.dagpenger.vedtak.modell.utbetaling.Utbetalingsdag.Companion.summer
import no.nav.dagpenger.vedtak.modell.visitor.VedtakVisitor
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

class Utbetalingsvedtak(
    vedtakId: UUID = UUID.randomUUID(),
    behandlingId: UUID,
    vedtakstidspunkt: LocalDateTime = LocalDateTime.now(),
    utfall: Boolean,
    virkningsdato: LocalDate,
    private val forbruk: Stønadsdager,
    private val utbetalingsdager: List<Utbetalingsdag>,
    private val trukketEgenandel: Beløp,
) : Vedtak(
    vedtakId = vedtakId,
    behandlingId = behandlingId,
    vedtakstidspunkt = vedtakstidspunkt,
    utfall = utfall,
    virkningsdato = virkningsdato,
) {

    companion object {
        fun utbetalingsvedtak(
            behandlingId: UUID,
            utfall: Boolean,
            virkningsdato: LocalDate,
            forbruk: Stønadsdager = Stønadsdager(dager = 0),
            utbetalingsdager: List<Utbetalingsdag> = emptyList(),
            trukketEgenandel: Beløp = 0.beløp,
        ) =
            Utbetalingsvedtak(
                behandlingId = behandlingId,
                utfall = utfall,
                virkningsdato = virkningsdato,
                forbruk = forbruk,
                utbetalingsdager = utbetalingsdager,
                trukketEgenandel = trukketEgenandel,
            )
    }
    override fun accept(visitor: VedtakVisitor) {
        val beløpTilUtbetaling = utbetalingsdager.summer() - trukketEgenandel
        visitor.visitLøpendeRettighet(
            vedtakId = vedtakId,
            behandlingId = behandlingId,
            vedtakstidspunkt = vedtakstidspunkt,
            utfall = utfall,
            virkningsdato = virkningsdato,
            forbruk = forbruk,
            trukketEgenandel = trukketEgenandel,
            beløpTilUtbetaling = beløpTilUtbetaling,
            utbetalingsdager = utbetalingsdager,
        )
    }
}
