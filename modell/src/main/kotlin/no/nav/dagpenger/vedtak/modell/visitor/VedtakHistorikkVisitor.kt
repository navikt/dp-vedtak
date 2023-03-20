package no.nav.dagpenger.vedtak.modell.visitor

interface VedtakHistorikkVisitor : VedtakVisitor {

    fun preVisitVedtak() {}
    fun postVisitVedtak() {}
}
