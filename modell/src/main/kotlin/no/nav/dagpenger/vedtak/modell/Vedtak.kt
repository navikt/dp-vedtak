package no.nav.dagpenger.vedtak.modell

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
        ) = Rammevedtak(
            behandlingId = behandlingId,
            virkningsdato = virkningsdato,
            vanligArbeidstidPerDag = vanligArbeidstidPerDag,
            grunnlag = grunnlag,
            dagsats = dagsats,
            stønadsperiode = stønadsperiode,
            dagpengerettighet = dagpengerettighet,
        )

        fun løpendeVedtak(behandlingId: UUID, utfall: Boolean, virkningsdato: LocalDate, forbruk: Tid) =
            LøpendeVedtak(
                behandlingId = behandlingId,
                utfall = utfall,
                virkningsdato = virkningsdato,
                forbruk = forbruk,
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
        )
        visitor.postVisitVedtak(vedtakId, behandlingId, virkningsdato, vedtakstidspunkt, utfall)
    }

    override fun populer(vedtakHistorikk: VedtakHistorikk) {
        vedtakHistorikk.dagsatshistorikk.put(virkningsdato, dagsats)
        vedtakHistorikk.stønadsperiodehistorikk.put(virkningsdato, stønadsperiode)
        vedtakHistorikk.grunnlaghistorikk.put(virkningsdato, grunnlag)
        vedtakHistorikk.gjenståendeStønadsperiode.put(virkningsdato, stønadsperiode)
        vedtakHistorikk.rettighet.put(virkningsdato, dagpengerettighet)
    }
}

class LøpendeVedtak(
    vedtakId: UUID = UUID.randomUUID(),
    behandlingId: UUID,
    vedtakstidspunkt: LocalDateTime = LocalDateTime.now(),
    utfall: Boolean,
    virkningsdato: LocalDate,
    private val forbruk: Tid,
) : Vedtak(
    vedtakId = vedtakId,
    behandlingId = behandlingId,
    vedtakstidspunkt = vedtakstidspunkt,
    utfall = utfall,
    virkningsdato = virkningsdato,
) {
    override fun accept(visitor: VedtakVisitor) {
        visitor.preVisitVedtak(vedtakId, behandlingId, virkningsdato, vedtakstidspunkt, utfall)
        visitor.visitForbruk(forbruk)
        visitor.postVisitVedtak(vedtakId, behandlingId, virkningsdato, vedtakstidspunkt, utfall)
    }

    override fun populer(vedtakHistorikk: VedtakHistorikk) {
        val gjenstående = vedtakHistorikk.gjenståendeStønadsperiode.get(virkningsdato)
        vedtakHistorikk.gjenståendeStønadsperiode.put(virkningsdato, gjenstående - forbruk)
    }
}
