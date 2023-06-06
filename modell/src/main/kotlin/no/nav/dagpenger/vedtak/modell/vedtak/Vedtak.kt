package no.nav.dagpenger.vedtak.modell.vedtak

import no.nav.dagpenger.vedtak.modell.Dagpengerettighet
import no.nav.dagpenger.vedtak.modell.entitet.Beløp
import no.nav.dagpenger.vedtak.modell.entitet.Stønadsdager
import no.nav.dagpenger.vedtak.modell.entitet.Timer
import no.nav.dagpenger.vedtak.modell.utbetaling.Betalingsdag
import no.nav.dagpenger.vedtak.modell.utbetaling.Betalingsdag.Companion.summer
import no.nav.dagpenger.vedtak.modell.visitor.VedtakVisitor
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

sealed class Vedtak(
    protected val vedtakId: UUID = UUID.randomUUID(),
    protected val behandlingId: UUID,
    protected val vedtakstidspunkt: LocalDateTime = LocalDateTime.now(),
    // @todo: Har alle vedtak utfall?
    protected val utfall: Boolean,
    protected val virkningsdato: LocalDate,
) : Comparable<Vedtak> {
    companion object {
        fun avslag(behandlingId: UUID, virkningsdato: LocalDate) =
            Avslag(behandlingId = behandlingId, virkningsdato = virkningsdato)

        fun innvilgelse(
            behandlingId: UUID,
            virkningsdato: LocalDate,
            grunnlag: BigDecimal,
            dagsats: BigDecimal,
            stønadsdager: Stønadsdager,
            dagpengerettighet: Dagpengerettighet,
            vanligArbeidstidPerDag: Timer,
            egenandel: Beløp,
        ) = Rammevedtak(
            behandlingId = behandlingId,
            virkningsdato = virkningsdato,
            vanligArbeidstidPerDag = vanligArbeidstidPerDag,
            grunnlag = grunnlag,
            dagsats = dagsats,
            stønadsdager = stønadsdager,
            dagpengerettighet = dagpengerettighet,
            egenandel = egenandel,
        )

        fun løpendeVedtak(
            behandlingId: UUID,
            utfall: Boolean,
            virkningsdato: LocalDate,
            forbruk: Stønadsdager,
            betalingsdager: List<Betalingsdag>,
            trukketEgenandel: Beløp,
            beløpTilUtbetaling: Beløp,
        ) =
            UtbetalingsVedtak(
                behandlingId = behandlingId,
                utfall = utfall,
                virkningsdato = virkningsdato,
                forbruk = forbruk,
                betalingsdager = betalingsdager,
                trukketEgenandel = trukketEgenandel,
                beløpTilUtbetaling = beløpTilUtbetaling,
            )

        internal fun Collection<Vedtak>.harBehandlet(behandlingId: UUID): Boolean =
            this.any { it.behandlingId == behandlingId }
    }

    abstract fun accept(visitor: VedtakVisitor)

    override fun compareTo(other: Vedtak): Int {
        return this.vedtakstidspunkt.compareTo(other.vedtakstidspunkt)
    }
}

class Avslag(
    vedtakId: UUID = UUID.randomUUID(),
    behandlingId: UUID,
    vedtakstidspunkt: LocalDateTime = LocalDateTime.now(),
    virkningsdato: LocalDate,
) : Vedtak(
    vedtakId = vedtakId,
    behandlingId = behandlingId,
    vedtakstidspunkt = vedtakstidspunkt,
    utfall = false,
    virkningsdato = virkningsdato,
) {
    override fun accept(visitor: VedtakVisitor) {
        visitor.visitAvslagVedtak(
            vedtakId = vedtakId,
            behandlingId = behandlingId,
            vedtakstidspunkt = vedtakstidspunkt,
            utfall = utfall,
            virkningsdato = virkningsdato,
        )
    }
}

class Rammevedtak(
    vedtakId: UUID = UUID.randomUUID(),
    behandlingId: UUID,
    vedtakstidspunkt: LocalDateTime = LocalDateTime.now(),
    virkningsdato: LocalDate,
    private val vanligArbeidstidPerDag: Timer,
    private val grunnlag: BigDecimal,
    private val dagsats: BigDecimal,
    private val stønadsdager: Stønadsdager,
    private val dagpengerettighet: Dagpengerettighet,
    private val egenandel: Beløp, // @todo: Beløp
) : Vedtak(
    vedtakId = vedtakId,
    behandlingId = behandlingId,
    vedtakstidspunkt = vedtakstidspunkt,
    utfall = true,
    virkningsdato = virkningsdato,
) {

    override fun accept(visitor: VedtakVisitor) {
        visitor.visitRammeVedtak(
            vedtakId = vedtakId,
            behandlingId = behandlingId,
            virkningsdato = virkningsdato,
            vedtakstidspunkt = vedtakstidspunkt,
            utfall = utfall,
            grunnlag = grunnlag,
            dagsats = dagsats,
            stønadsdager = stønadsdager,
            vanligArbeidstidPerDag = vanligArbeidstidPerDag,
            dagpengerettighet = dagpengerettighet,
            egenandel = egenandel,
        )
    }
}

class UtbetalingsVedtak(
    vedtakId: UUID = UUID.randomUUID(),
    behandlingId: UUID,
    vedtakstidspunkt: LocalDateTime = LocalDateTime.now(),
    utfall: Boolean,
    virkningsdato: LocalDate,
    private val forbruk: Stønadsdager,
    private val betalingsdager: List<Betalingsdag>,
    private val trukketEgenandel: Beløp,
    private val beløpTilUtbetaling: Beløp,
) : Vedtak(
    vedtakId = vedtakId,
    behandlingId = behandlingId,
    vedtakstidspunkt = vedtakstidspunkt,
    utfall = utfall,
    virkningsdato = virkningsdato,
) {
    override fun accept(visitor: VedtakVisitor) {
        val beløpTilUtbetaling = betalingsdager.summer() - trukketEgenandel
        visitor.visitUtbetalingsVedtak(
            vedtakId = vedtakId,
            behandlingId = behandlingId,
            vedtakstidspunkt = vedtakstidspunkt,
            utfall = utfall,
            virkningsdato = virkningsdato,
            forbruk = forbruk,
            trukketEgenandel = trukketEgenandel,
            beløpTilUtbetaling = beløpTilUtbetaling,
        )
    }
}

class StansVedtak(
    vedtakId: UUID = UUID.randomUUID(),
    behandlingId: UUID,
    vedtakstidspunkt: LocalDateTime = LocalDateTime.now(),
    virkningsdato: LocalDate,
) : Vedtak(vedtakId, behandlingId, vedtakstidspunkt, utfall = false, virkningsdato) {
    override fun accept(visitor: VedtakVisitor) {
        visitor.visitStansVedtak(
            vedtakId,
            behandlingId,
            virkningsdato,
            vedtakstidspunkt,
            utfall,
        )
    }
}
