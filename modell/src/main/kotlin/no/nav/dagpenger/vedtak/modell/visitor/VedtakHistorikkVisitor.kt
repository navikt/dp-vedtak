package no.nav.dagpenger.vedtak.modell.visitor

interface VedtakHistorikkVisitor : VedtakVisitor {
    fun preVisitVedtakHistorikk() {}

    fun postVisitVedtakHistorikk() {}
}
