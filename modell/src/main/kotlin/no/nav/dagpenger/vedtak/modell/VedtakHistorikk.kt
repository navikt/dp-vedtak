package no.nav.dagpenger.vedtak.modell

import no.nav.dagpenger.vedtak.modell.Vedtak.Companion.finn
import no.nav.dagpenger.vedtak.modell.entitet.Timer
import no.nav.dagpenger.vedtak.modell.mengde.Stønadsperiode
import no.nav.dagpenger.vedtak.modell.rapportering.LøpendeBehandling
import no.nav.dagpenger.vedtak.modell.rapportering.Rapporteringsperiode
import no.nav.dagpenger.vedtak.modell.visitor.VedtakHistorikkVisitor
import java.math.BigDecimal
import java.time.LocalDate

internal class VedtakHistorikk(historiskeVedtak: List<Vedtak> = listOf()) {
    private val vedtak = historiskeVedtak.toMutableList()

    internal val dagsatshistorikk = TemporalCollection<BigDecimal>()
    internal val grunnlaghistorikk = TemporalCollection<BigDecimal>()
    internal val stønadsperiodehistorikk = TemporalCollection<Stønadsperiode>()
    internal val gjenståendeStønadsperiode = TemporalCollection<Stønadsperiode>()
    internal val dagpengerRettighetHistorikk = TemporalCollection<Dagpengerettighet>()
    internal val vanligArbeidstidHistorikk = TemporalCollection<Timer>()

    init {
        vedtak.forEach { it.populer(this) }
    }

    internal fun leggTilVedtak(vedtak: Vedtak) {
        this.vedtak.add(vedtak.also { it.populer(this) })
    }

    fun accept(visitor: VedtakHistorikkVisitor) {
        if (gjenståendeStønadsperiode.harHistorikk()) {
            visitor.visitGjenståendeStønadsperiode(gjenståendeStønadsperiode.get(LocalDate.now()))
        }
        visitor.preVisitVedtak()
        vedtak.forEach { it.accept(visitor) }
        visitor.postVisitVedtak()
    }

    fun håndter(rapporteringsperiode: Rapporteringsperiode) {
        this.leggTilVedtak(
            LøpendeBehandling(
                rapporteringsId = rapporteringsperiode.rapporteringsId,
                satshistorikk = dagsatshistorikk,
                rettighethistorikk = dagpengerRettighetHistorikk,
                vanligarbeidstidhistorikk = vanligArbeidstidHistorikk,
            ).håndter(rapporteringsperiode),
        )
    }

    fun harVedtak(dato: LocalDate = LocalDate.now()) = vedtak.finn(dato) != null
}
