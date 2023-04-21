package no.nav.dagpenger.vedtak.modell.visitor

import no.nav.dagpenger.vedtak.modell.mengde.Stønadsperiode
import java.math.BigDecimal

interface VedtakHistorikkVisitor : VedtakVisitor {

    fun visitGjenståendeStønadsperiode(gjenståendePeriode: Stønadsperiode) {}
    fun visitGjenståendeEgenandel(gjenståendeEgenandel: BigDecimal) {}
    fun preVisitVedtak() {}
    fun postVisitVedtak() {}
}
