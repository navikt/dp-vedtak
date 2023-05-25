package no.nav.dagpenger.vedtak.modell.visitor

import no.nav.dagpenger.vedtak.modell.Dagpengerettighet
import no.nav.dagpenger.vedtak.modell.entitet.Beløp
import no.nav.dagpenger.vedtak.modell.entitet.Stønadsdager
import no.nav.dagpenger.vedtak.modell.entitet.Timer
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

interface VedtakVisitor {
    fun preVisitVedtak(
        vedtakId: UUID,
        behandlingId: UUID,
        virkningsdato: LocalDate,
        vedtakstidspunkt: LocalDateTime,
        utfall: Boolean,
    ) {
    }

    fun visitRammeVedtak(
        grunnlag: BigDecimal,
        dagsats: BigDecimal,
        stønadsdager: Stønadsdager,
        vanligArbeidstidPerDag: Timer,
        dagpengerettighet: Dagpengerettighet,
        egenandel: Beløp,
    ) {
    }

    fun visitLøpendeVedtak(forbruk: Stønadsdager, beløpTilUtbetaling: Beløp, trukketEgenandel: Beløp) {}

    fun postVisitVedtak(
        vedtakId: UUID,
        behandlingId: UUID,
        virkningsdato: LocalDate,
        vedtakstidspunkt: LocalDateTime,
        utfall: Boolean,
    ) {
    }
}
