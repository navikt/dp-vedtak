package no.nav.dagpenger.vedtak.modell

import no.nav.dagpenger.vedtak.modell.Vedtak.Companion.finn
import no.nav.dagpenger.vedtak.modell.entitet.Timer
import no.nav.dagpenger.vedtak.modell.mengde.Stønadsperiode
import no.nav.dagpenger.vedtak.modell.mengde.Tid
import no.nav.dagpenger.vedtak.modell.visitor.VedtakHistorikkVisitor
import no.nav.dagpenger.vedtak.modell.visitor.VedtakVisitor
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

internal class VedtakHistorikk(private val vedtak: MutableList<Vedtak> = mutableListOf()) {

    internal val dagsatshistorikk = TemporalCollection<BigDecimal>()
    internal val grunnlaghistorikk = TemporalCollection<BigDecimal>()
    internal val stønadsperiodehistorikk = TemporalCollection<Stønadsperiode>()
    internal val gjenståendeStønadsperiode = TemporalCollection<Stønadsperiode>()

    fun leggTilVedtak(vedtak: Vedtak) {
        this.vedtak.add(vedtak)
        OppdaterVedtakFakta(vedtak, this)
    }

    fun accept(visitor: VedtakHistorikkVisitor) {
        if (gjenståendeStønadsperiode.harHistorikk()) {
            visitor.visitGjenståendeStønadsperiode(gjenståendeStønadsperiode.get(LocalDate.now()))
        }
        visitor.preVisitVedtak()
        vedtak.forEach { it.accept(visitor) }
        visitor.postVisitVedtak()
    }

    fun harVedtak(dato: LocalDate = LocalDate.now()) = vedtak.finn(dato) != null

    private class OppdaterVedtakFakta(vedtak: Vedtak, private val vedtakHistorikk: VedtakHistorikk) : VedtakVisitor {
        init {
            vedtak.accept(this)
        }

        lateinit var virkningsdato: LocalDate
        override fun preVisitVedtak(
            vedtakId: UUID,
            behandlingId: UUID,
            virkningsdato: LocalDate,
            vedtakstidspunkt: LocalDateTime,
            utfall: Boolean,
        ) {
            this.virkningsdato = virkningsdato
        }

        override fun visitRammeVedtak(
            grunnlag: BigDecimal,
            dagsats: BigDecimal,
            stønadsperiode: Stønadsperiode,
            vanligArbeidstidPerDag: Timer,
            dagpengerettighet: Dagpengerettighet,
        ) {
            vedtakHistorikk.dagsatshistorikk.put(virkningsdato, dagsats)
            vedtakHistorikk.stønadsperiodehistorikk.put(virkningsdato, stønadsperiode)
            vedtakHistorikk.gjenståendeStønadsperiode.put(virkningsdato, stønadsperiode)
            vedtakHistorikk.grunnlaghistorikk.put(virkningsdato, grunnlag)
        }

        override fun visitForbruk(forbruk: Tid) {
            val gjenstående = vedtakHistorikk.gjenståendeStønadsperiode.get(virkningsdato)
            vedtakHistorikk.gjenståendeStønadsperiode.put(virkningsdato, gjenstående - forbruk)
        }
    }
}
