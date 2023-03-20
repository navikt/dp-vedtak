package no.nav.dagpenger.vedtak.modell

import no.nav.dagpenger.vedtak.modell.mengde.Stønadsperiode
import no.nav.dagpenger.vedtak.modell.visitor.VedtakHistorikkVisitor
import java.math.BigDecimal

internal class VedtakHistorikk(private val vedtak: MutableList<Vedtak> = mutableListOf()) {

    internal val dagsatshistorikk = TemporalCollection<BigDecimal>()
    internal val grunnlaghistorikk = TemporalCollection<BigDecimal>()
    internal val stønadsperiodehistorikk = TemporalCollection<Stønadsperiode>()
    internal val gjensteåndeStønadsperiode = TemporalCollection<Stønadsperiode>()

    fun leggTilVedtak(vedtak: Vedtak) {
        this.vedtak.add(vedtak)
        // OppdaterVedtakFakta(vedtak, this)
    }

    fun accept(visitor: VedtakHistorikkVisitor) {
        visitor.preVisitVedtak()
        vedtak.forEach { it.accept(visitor) }
        visitor.postVisitVedtak()
    }

    // fun harVedtak(rapporteringsHendelse: Rapporteringshendelse) = vedtak.isNotEmpty()

    /*private class OppdaterVedtakFakta(vedtak: Vedtak, private val vedtakHistorikk: VedtakHistorikk) : VedtakVisitor {
        init {
            vedtak.accept(this)
        }

        lateinit var virkningsdato: LocalDate
        override fun preVisitVedtak(
            vedtakId: UUID,
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
            fastsattArbeidstidPerDag: Timer,
            dagpengerettighet: Dagpengerettighet,
            gyldigTom: LocalDate?,
        ) {
            vedtakHistorikk.dagsatshistorikk.put(virkningsdato, dagsats)
            vedtakHistorikk.stønadsperiodehistorikk.put(virkningsdato, stønadsperiode)
            vedtakHistorikk.gjensteåndeStønadsperiode.put(virkningsdato, stønadsperiode)
            vedtakHistorikk.grunnlaghistorikk.put(virkningsdato, grunnlag)
        }

        override fun visitForbruk(forbruk: Tid) {
            val gjenstående = vedtakHistorikk.gjensteåndeStønadsperiode.get(virkningsdato)
            vedtakHistorikk.gjensteåndeStønadsperiode.put(virkningsdato, gjenstående - forbruk)
        }
    }*/
}
