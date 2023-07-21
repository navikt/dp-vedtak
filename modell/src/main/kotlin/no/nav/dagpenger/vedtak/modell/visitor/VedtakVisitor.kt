package no.nav.dagpenger.vedtak.modell.visitor

import no.nav.dagpenger.vedtak.modell.Dagpengerettighet
import no.nav.dagpenger.vedtak.modell.entitet.Beløp
import no.nav.dagpenger.vedtak.modell.entitet.Stønadsdager
import no.nav.dagpenger.vedtak.modell.entitet.Timer
import no.nav.dagpenger.vedtak.modell.utbetaling.Utbetalingsdag
import no.nav.dagpenger.vedtak.modell.vedtak.Vedtak
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

interface VedtakVisitor {

    fun visitRammevedtak(
        vedtakId: UUID,
        behandlingId: UUID,
        virkningsdato: LocalDate,
        vedtakstidspunkt: LocalDateTime,
        utfall: Boolean,
        grunnlag: BigDecimal,
        dagsats: Beløp,
        stønadsdager: Stønadsdager,
        vanligArbeidstidPerDag: Timer,
        dagpengerettighet: Dagpengerettighet,
        egenandel: Beløp,
        tilstand: Vedtak.Tilstand,
    ) {}

    fun visitLøpendeRettighet(
        vedtakId: UUID,
        behandlingId: UUID,
        vedtakstidspunkt: LocalDateTime,
        utfall: Boolean,
        virkningsdato: LocalDate,
        forbruk: Stønadsdager,
        trukketEgenandel: Beløp,
        beløpTilUtbetaling: Beløp,
        rettighetsdager: List<Utbetalingsdag>,
    ) {}

    fun visitAvslag(
        vedtakId: UUID,
        behandlingId: UUID,
        vedtakstidspunkt: LocalDateTime,
        utfall: Boolean,
        virkningsdato: LocalDate,
    ) {}

    fun visitStans(
        vedtakId: UUID,
        behandlingId: UUID,
        virkningsdato: LocalDate,
        vedtakstidspunkt: LocalDateTime,
        utfall: Boolean,
    ) {}
}
