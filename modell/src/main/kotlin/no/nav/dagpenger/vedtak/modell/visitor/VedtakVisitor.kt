package no.nav.dagpenger.vedtak.modell.visitor

import no.nav.dagpenger.vedtak.modell.SakId
import no.nav.dagpenger.vedtak.modell.entitet.Beløp
import no.nav.dagpenger.vedtak.modell.entitet.Stønadsdager
import no.nav.dagpenger.vedtak.modell.utbetaling.Utbetalingsdag
import no.nav.dagpenger.vedtak.modell.vedtak.Vedtak
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

interface VedtakVisitor : FaktumVisitor, RettighetVisitor {

    fun preVisitVedtak(
        vedtakId: UUID,
        sakId: SakId,
        behandlingId: UUID,
        virkningsdato: LocalDate,
        vedtakstidspunkt: LocalDateTime,
        type: Vedtak.VedtakType,
    ) {}

    fun postVisitVedtak(
        vedtakId: UUID,
        sakId: SakId,
        behandlingId: UUID,
        virkningsdato: LocalDate,
        vedtakstidspunkt: LocalDateTime,
        type: Vedtak.VedtakType,
    ) {}

    fun visitUtbetalingsvedtak(
        vedtakId: UUID,
        utfall: Boolean,
        forbruk: Stønadsdager,
        beløpTilUtbetaling: Beløp,
        utbetalingsdager: List<Utbetalingsdag>,
    ) {}

    fun visitAvslag(
        vedtakId: UUID,
        sakId: SakId,
        behandlingId: UUID,
        vedtakstidspunkt: LocalDateTime,
        utfall: Boolean,
        virkningsdato: LocalDate,
    ) {}

    fun visitStans(
        vedtakId: UUID,
        sakId: SakId,
        behandlingId: UUID,
        virkningsdato: LocalDate,
        vedtakstidspunkt: LocalDateTime,
        utfall: Boolean?,
    ) {}
}
