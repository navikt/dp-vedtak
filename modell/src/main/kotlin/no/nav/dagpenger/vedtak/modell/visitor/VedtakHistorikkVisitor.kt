package no.nav.dagpenger.vedtak.modell.visitor

import no.nav.dagpenger.vedtak.modell.entitet.Beløp
import no.nav.dagpenger.vedtak.modell.entitet.Stønadsdager

interface VedtakHistorikkVisitor : VedtakVisitor {
    fun visitGjenståendeStønadsperiode(gjenståendePeriode: Stønadsdager) {}
    fun visitGjenståendeEgenandel(gjenståendeEgenandel: Beløp) {}
    fun preVisitVedtak() {}
    fun postVisitVedtak() {}
}
