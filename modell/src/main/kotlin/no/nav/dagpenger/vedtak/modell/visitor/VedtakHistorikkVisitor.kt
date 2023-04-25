package no.nav.dagpenger.vedtak.modell.visitor

import no.nav.dagpenger.vedtak.modell.entitet.Beløp
import no.nav.dagpenger.vedtak.modell.mengde.Stønadsperiode

interface VedtakHistorikkVisitor : VedtakVisitor {

    fun visitGjenståendeStønadsperiode(gjenståendePeriode: Stønadsperiode) {}
    fun visitGjenståendeEgenandel(gjenståendeEgenandel: Beløp) {}
    fun preVisitVedtak() {}
    fun postVisitVedtak() {}
}
