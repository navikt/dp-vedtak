package no.nav.dagpenger.vedtak.modell.vedtak

import no.nav.dagpenger.vedtak.modell.Dagpengerettighet
import no.nav.dagpenger.vedtak.modell.entitet.Beløp
import no.nav.dagpenger.vedtak.modell.entitet.Stønadsdager
import no.nav.dagpenger.vedtak.modell.entitet.Timer
import no.nav.dagpenger.vedtak.modell.utbetaling.LøpendeRettighetDag
import no.nav.dagpenger.vedtak.modell.vedtak.VedtakObserver.LøpendeVedtakFattet
import no.nav.dagpenger.vedtak.modell.vedtak.VedtakObserver.RammevedtakFattet
import no.nav.dagpenger.vedtak.modell.vedtak.VedtakObserver.Utfall.Avslått
import no.nav.dagpenger.vedtak.modell.vedtak.VedtakObserver.Utfall.Innvilget
import no.nav.dagpenger.vedtak.modell.visitor.VedtakVisitor
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

internal class VedtakFattetVisitor : VedtakVisitor {

    lateinit var rammevedtakFattet: RammevedtakFattet
    lateinit var løpendeVedtakFattet: LøpendeVedtakFattet

    override fun visitRammevedtak(
        vedtakId: UUID,
        behandlingId: UUID,
        virkningsdato: LocalDate,
        vedtakstidspunkt: LocalDateTime,
        utfall: Boolean,
        grunnlag: BigDecimal,
        dagsats: BigDecimal,
        stønadsdager: Stønadsdager,
        vanligArbeidstidPerDag: Timer,
        dagpengerettighet: Dagpengerettighet,
        egenandel: Beløp,
    ) {
        rammevedtakFattet = RammevedtakFattet(
            vedtakId = vedtakId,
            vedtakstidspunkt = vedtakstidspunkt,
            behandlingId = behandlingId,
            virkningsdato = virkningsdato,
            utfall = when (utfall) {
                true -> Innvilget
                false -> Avslått
            },
        )
    }

    override fun visitLøpendeRettighet(
        vedtakId: UUID,
        behandlingId: UUID,
        vedtakstidspunkt: LocalDateTime,
        utfall: Boolean,
        virkningsdato: LocalDate,
        forbruk: Stønadsdager,
        trukketEgenandel: Beløp,
        beløpTilUtbetaling: Beløp,
        rettighetsdager: List<LøpendeRettighetDag>,
    ) {
        løpendeVedtakFattet = LøpendeVedtakFattet(
            vedtakId = vedtakId,
            vedtakstidspunkt = vedtakstidspunkt,
            behandlingId = behandlingId,
            virkningsdato = virkningsdato,
            utbetalingsdager = rettighetsdager,
            utfall = when (utfall) {
                true -> Innvilget
                false -> Avslått
            },
        )
    }

    override fun visitAvslag(
        vedtakId: UUID,
        behandlingId: UUID,
        vedtakstidspunkt: LocalDateTime,
        utfall: Boolean,
        virkningsdato: LocalDate,
    ) {
        rammevedtakFattet = RammevedtakFattet(
            vedtakId = vedtakId,
            vedtakstidspunkt = vedtakstidspunkt,
            behandlingId = behandlingId,
            virkningsdato = virkningsdato,
            utfall = when (utfall) {
                true -> Innvilget
                false -> Avslått
            },
        )
    }

    override fun visitStans(
        vedtakId: UUID,
        behandlingId: UUID,
        virkningsdato: LocalDate,
        vedtakstidspunkt: LocalDateTime,
        utfall: Boolean,
    ) {
        rammevedtakFattet = RammevedtakFattet(
            vedtakId = vedtakId,
            vedtakstidspunkt = vedtakstidspunkt,
            behandlingId = behandlingId,
            virkningsdato = virkningsdato,
            utfall = when (utfall) {
                true -> Innvilget
                false -> Avslått
            },
        )
    }
}
