package no.nav.dagpenger.vedtak.modell.visitor

import no.nav.dagpenger.vedtak.modell.entitet.Beløp
import no.nav.dagpenger.vedtak.modell.entitet.Stønadsdager
import no.nav.dagpenger.vedtak.modell.utbetaling.Utbetalingsdag
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

interface VedtakVisitor : FaktumVisitor, RettighetVisitor {

    fun preVisitVedtak(
        vedtakId: UUID,
        behandlingId: UUID,
        virkningsdato: LocalDate,
        vedtakstidspunkt: LocalDateTime,
    ) {}

    fun postVisitVedtak(
        vedtakId: UUID,
        behandlingId: UUID,
        virkningsdato: LocalDate,
        vedtakstidspunkt: LocalDateTime,
    ) {}

    fun visitLøpendeRettighet(
        vedtakId: UUID,
        behandlingId: UUID,
        vedtakstidspunkt: LocalDateTime,
        utfall: Boolean?,
        virkningsdato: LocalDate,
        forbruk: Stønadsdager,
        trukketEgenandel: Beløp,
        beløpTilUtbetaling: Beløp,
        utbetalingsdager: List<Utbetalingsdag>,
    ) {}

    fun visitAvslag(
        vedtakId: UUID,
        behandlingId: UUID,
        vedtakstidspunkt: LocalDateTime,
        utfall: Boolean?,
        virkningsdato: LocalDate,
    ) {}

    fun visitStans(
        vedtakId: UUID,
        behandlingId: UUID,
        virkningsdato: LocalDate,
        vedtakstidspunkt: LocalDateTime,
        utfall: Boolean?,
    ) {}
}
