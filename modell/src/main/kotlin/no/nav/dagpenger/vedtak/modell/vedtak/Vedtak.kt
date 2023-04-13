package no.nav.dagpenger.vedtak.modell.vedtak

import no.nav.dagpenger.vedtak.modell.Beløp
import no.nav.dagpenger.vedtak.modell.Dagpengerettighet
import no.nav.dagpenger.vedtak.modell.entitet.Timer
import no.nav.dagpenger.vedtak.modell.entitet.Timer.Companion.timer
import no.nav.dagpenger.vedtak.modell.mengde.Stønadsperiode
import no.nav.dagpenger.vedtak.modell.mengde.Tid
import no.nav.dagpenger.vedtak.modell.visitor.VedtakVisitor
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

sealed class Vedtak(
    protected val vedtakId: UUID = UUID.randomUUID(),
    protected val behandlingId: UUID,
    protected val vedtakstidspunkt: LocalDateTime = LocalDateTime.now(),
    protected val utfall: Boolean,
    protected val virkningsdato: LocalDate,
) {
    companion object {
        fun avslag(behandlingId: UUID, virkningsdato: LocalDate) =
            Avslag(behandlingId = behandlingId, virkningsdato = virkningsdato)

        fun innvilgelse(
            behandlingId: UUID,
            virkningsdato: LocalDate,
            grunnlag: BigDecimal,
            dagsats: BigDecimal,
            stønadsperiode: Stønadsperiode,
            dagpengerettighet: Dagpengerettighet,
            vanligArbeidstidPerDag: Timer,
            antallVenteDager: Double,
        ) = Rammevedtak(
            behandlingId = behandlingId,
            virkningsdato = virkningsdato,
            vanligArbeidstidPerDag = vanligArbeidstidPerDag,
            grunnlag = grunnlag,
            dagsats = dagsats,
            stønadsperiode = stønadsperiode,
            dagpengerettighet = dagpengerettighet,
            antallVenteDager = antallVenteDager,
        )

        fun løpendeVedtak(behandlingId: UUID, utfall: Boolean, virkningsdato: LocalDate, forbruk: Tid, beløpTilUtbetaling: Beløp) =
            LøpendeVedtak(
                behandlingId = behandlingId,
                utfall = utfall,
                virkningsdato = virkningsdato,
                forbruk = forbruk,
                beløpTilUtbetaling = beløpTilUtbetaling,
            )

        fun Collection<Vedtak>.finn(dato: LocalDate) = this.find { it.virkningsdato <= dato }
    }

    abstract fun accept(visitor: VedtakVisitor)
    internal abstract fun populer(vedtakHistorikk: VedtakHistorikk)
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
        visitor.preVisitVedtak(vedtakId, behandlingId, virkningsdato, vedtakstidspunkt, utfall)
        visitor.postVisitVedtak(
            vedtakId,
            behandlingId,
            virkningsdato,
            vedtakstidspunkt,
            utfall,
        )
    }

    override fun populer(vedtakHistorikk: VedtakHistorikk) {
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
    private val stønadsperiode: Stønadsperiode,
    private val dagpengerettighet: Dagpengerettighet,
    private val antallVenteDager: Double,
) : Vedtak(
    vedtakId = vedtakId,
    behandlingId = behandlingId,
    vedtakstidspunkt = vedtakstidspunkt,
    utfall = true,
    virkningsdato = virkningsdato,
) {

    private val ventetid = vanligArbeidstidPerDag * antallVenteDager
    override fun accept(visitor: VedtakVisitor) {
        visitor.preVisitVedtak(vedtakId, behandlingId, virkningsdato, vedtakstidspunkt, utfall)
        visitor.visitRammeVedtak(
            vanligArbeidstidPerDag = vanligArbeidstidPerDag,
            grunnlag = grunnlag,
            dagsats = dagsats,
            stønadsperiode = stønadsperiode,
            dagpengerettighet = dagpengerettighet,
        )
        visitor.postVisitVedtak(vedtakId, behandlingId, virkningsdato, vedtakstidspunkt, utfall)
    }

    override fun populer(vedtakHistorikk: VedtakHistorikk) {
        vedtakHistorikk.dagsatsHistorikk.put(virkningsdato, dagsats)
        vedtakHistorikk.stønadsperiodeHistorikk.put(virkningsdato, stønadsperiode)
        vedtakHistorikk.grunnlagHistorikk.put(virkningsdato, grunnlag)
        vedtakHistorikk.gjenståendeStønadsperiodeHistorikk.put(virkningsdato, stønadsperiode)
        vedtakHistorikk.dagpengerettighetHistorikk.put(virkningsdato, dagpengerettighet)
        vedtakHistorikk.vanligArbeidstidHistorikk.put(virkningsdato, vanligArbeidstidPerDag)
        vedtakHistorikk.ventetidHistorikk.put(virkningsdato, ventetid)
        vedtakHistorikk.gjenståendeVentetidHistorikk.put(virkningsdato, ventetid)
    }
}

class LøpendeVedtak(
    vedtakId: UUID = UUID.randomUUID(),
    behandlingId: UUID,
    vedtakstidspunkt: LocalDateTime = LocalDateTime.now(),
    utfall: Boolean,
    virkningsdato: LocalDate,
    private val forbruk: Tid,
    private val beløpTilUtbetaling: Beløp,
) : Vedtak(
    vedtakId = vedtakId,
    behandlingId = behandlingId,
    vedtakstidspunkt = vedtakstidspunkt,
    utfall = utfall,
    virkningsdato = virkningsdato,
) {
    override fun accept(visitor: VedtakVisitor) {
        visitor.preVisitVedtak(vedtakId, behandlingId, virkningsdato, vedtakstidspunkt, utfall)
        visitor.visitLøpendeVedtak(forbruk, beløpTilUtbetaling)
        visitor.postVisitVedtak(vedtakId, behandlingId, virkningsdato, vedtakstidspunkt, utfall)
    }

    override fun populer(vedtakHistorikk: VedtakHistorikk) {
        val gjenstående = vedtakHistorikk.gjenståendeStønadsperiodeHistorikk.get(virkningsdato)
        vedtakHistorikk.gjenståendeStønadsperiodeHistorikk.put(virkningsdato, gjenstående - forbruk)
        vedtakHistorikk.gjenståendeVentetidHistorikk.put(virkningsdato, 0.timer)
    }
}
