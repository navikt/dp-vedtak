package no.nav.dagpenger.vedtak.modell.visitor

import no.nav.dagpenger.vedtak.modell.Beløp
import java.time.LocalDate

interface VedtakHistorikkVisitor : VedtakVisitor {
    fun preVisitVedtakHistorikk() {}

    fun postVisitVedtakHistorikk() {}

    fun visitDagsatsHistorikk(dato: LocalDate, dagsats: Beløp) {}

    fun visitGjeldendeDagsats(dagsats: Beløp) {}
}
