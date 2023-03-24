package no.nav.dagpenger.vedtak.modell

import no.nav.dagpenger.vedtak.modell.Vedtak.Companion.finn
import no.nav.dagpenger.vedtak.modell.hendelser.Rapporteringshendelse
import no.nav.dagpenger.vedtak.modell.mengde.Stønadsperiode
import no.nav.dagpenger.vedtak.modell.visitor.VedtakHistorikkVisitor
import java.math.BigDecimal
import java.time.LocalDate

internal class VedtakHistorikk(historiskeVedtak: List<Vedtak> = listOf()) {
    private val vedtak = historiskeVedtak.toMutableList()

    internal val dagsatshistorikk = TemporalCollection<BigDecimal>()
    internal val grunnlaghistorikk = TemporalCollection<BigDecimal>()
    internal val stønadsperiodehistorikk = TemporalCollection<Stønadsperiode>()
    internal val gjenståendeStønadsperiode = TemporalCollection<Stønadsperiode>()
    internal val rettighet = TemporalCollection<Dagpengerettighet>()

    init {
        vedtak.forEach { it.populer(this) }
    }

    fun leggTilVedtak(vedtak: Vedtak) {
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

    fun håndter(rapporteringshendelse: Rapporteringshendelse): Behandling<*> {
        // finn vilkår og fastsettelser
        // Terskel(rettighet)
        // SatsFastsettelse(dagsatshistorikk)
        //
        // lag behandling

        // Rapporteringsbehandling(rapporteringsId, vilkår = listOf(Terskel(rettighet), fastsettelser = listOf(SatsFastsettelse(dagsatshistorikk)))
        TODO()
    }

    fun harVedtak(dato: LocalDate = LocalDate.now()) = vedtak.finn(dato) != null
}
