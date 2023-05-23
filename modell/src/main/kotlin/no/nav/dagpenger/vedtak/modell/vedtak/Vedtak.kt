package no.nav.dagpenger.vedtak.modell.vedtak

import no.nav.dagpenger.vedtak.modell.Dagpengerettighet
import no.nav.dagpenger.vedtak.modell.entitet.Beløp
import no.nav.dagpenger.vedtak.modell.entitet.Timer
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
            stønadsperiode: Stønadsperiode,
            dagpengerettighet: Dagpengerettighet,
            vanligArbeidstidPerDag: Timer,
            egenandel: Beløp,
        ) = Rammevedtak(
            behandlingId = behandlingId,
            virkningsdato = virkningsdato,
            vanligArbeidstidPerDag = vanligArbeidstidPerDag,
            grunnlag = grunnlag,
            dagsats = dagsats,
            stønadsperiode = stønadsperiode,
            dagpengerettighet = dagpengerettighet,
            egenandel = egenandel,
        )

        fun løpendeVedtak(
            behandlingId: UUID,
            utfall: Boolean,
            virkningsdato: LocalDate,
            forbruk: Tid,
            beløpTilUtbetaling: Beløp,
            trukketEgenandel: Beløp,
        ) =
            LøpendeVedtak(
                behandlingId = behandlingId,
                utfall = utfall,
                virkningsdato = virkningsdato,
                forbruk = forbruk,
                beløpTilUtbetaling = beløpTilUtbetaling,
                trukketEgenandel = trukketEgenandel,
            )

        internal fun Collection<Vedtak>.harBehandlet(behandlingId: UUID): Boolean =
            this.any { it.behandlingId == behandlingId }
    }

    abstract fun accept(visitor: VedtakVisitor)
    internal abstract fun populer(vedtakHistorikk: VedtakHistorikk)

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
    private val egenandel: Beløp, // @todo: Beløp
) : Vedtak(
    vedtakId = vedtakId,
    behandlingId = behandlingId,
    vedtakstidspunkt = vedtakstidspunkt,
    utfall = true,
    virkningsdato = virkningsdato,
) {

    override fun accept(visitor: VedtakVisitor) {
        visitor.preVisitVedtak(vedtakId, behandlingId, virkningsdato, vedtakstidspunkt, utfall)
        visitor.visitRammeVedtak(
            vanligArbeidstidPerDag = vanligArbeidstidPerDag,
            grunnlag = grunnlag,
            dagsats = dagsats,
            stønadsperiode = stønadsperiode,
            dagpengerettighet = dagpengerettighet,
            egenandel = egenandel,
        )
        visitor.postVisitVedtak(vedtakId, behandlingId, virkningsdato, vedtakstidspunkt, utfall)
    }

    override fun populer(vedtakHistorikk: VedtakHistorikk) {
        vedtakHistorikk.dagsatsHistorikk.put(virkningsdato, dagsats)
        vedtakHistorikk.grunnlagHistorikk.put(virkningsdato, grunnlag)
        vedtakHistorikk.stønadsperiodeHistorikk.put(virkningsdato, stønadsperiode)
        vedtakHistorikk.dagpengerettighetHistorikk.put(virkningsdato, dagpengerettighet)
        vedtakHistorikk.vanligArbeidstidHistorikk.put(virkningsdato, vanligArbeidstidPerDag)
        vedtakHistorikk.egenandelHistorikk.put(virkningsdato, egenandel)
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
    private val trukketEgenandel: Beløp,
) : Vedtak(
    vedtakId = vedtakId,
    behandlingId = behandlingId,
    vedtakstidspunkt = vedtakstidspunkt,
    utfall = utfall,
    virkningsdato = virkningsdato,
) {
    override fun accept(visitor: VedtakVisitor) {
        visitor.preVisitVedtak(vedtakId, behandlingId, virkningsdato, vedtakstidspunkt, utfall)
        visitor.visitLøpendeVedtak(forbruk, beløpTilUtbetaling, trukketEgenandel)
        visitor.postVisitVedtak(vedtakId, behandlingId, virkningsdato, vedtakstidspunkt, utfall)
    }

    override fun populer(vedtakHistorikk: VedtakHistorikk) {
        vedtakHistorikk.forbrukHistorikk.put(virkningsdato, forbruk)
        vedtakHistorikk.trukketEgenandelHistorikk.put(virkningsdato, trukketEgenandel)
    }
}

class StansVedtak(
    vedtakId: UUID = UUID.randomUUID(),
    behandlingId: UUID,
    vedtakstidspunkt: LocalDateTime = LocalDateTime.now(),
    virkningsdato: LocalDate,
) : Vedtak(vedtakId, behandlingId, vedtakstidspunkt, utfall = false, virkningsdato) {
    override fun accept(visitor: VedtakVisitor) {
        visitor.preVisitVedtak(vedtakId, behandlingId, virkningsdato, vedtakstidspunkt, utfall)
        visitor.postVisitVedtak(vedtakId, behandlingId, virkningsdato, vedtakstidspunkt, utfall)
    }

    override fun populer(vedtakHistorikk: VedtakHistorikk) {
        vedtakHistorikk.dagpengerettighetHistorikk.put(virkningsdato, Dagpengerettighet.Ingen)
    }
}
