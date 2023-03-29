package no.nav.dagpenger.vedtak.modell.visitor

import no.nav.dagpenger.vedtak.modell.entitet.Timer
import no.nav.dagpenger.vedtak.modell.mengde.Stønadsperiode

interface VedtakHistorikkVisitor : VedtakVisitor {

    fun visitGjenståendeStønadsperiode(gjenståendePeriode: Stønadsperiode) {}
    fun visitGjenståendeVentetid(gjenståendeVentetid: Timer) {}
    fun preVisitVedtak() {}
    fun postVisitVedtak() {}
}
