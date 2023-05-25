package no.nav.dagpenger.vedtak.modell.rapportering

import no.nav.dagpenger.vedtak.modell.Dagpengerettighet
import no.nav.dagpenger.vedtak.modell.TemporalCollection
import no.nav.dagpenger.vedtak.modell.entitet.Beløp
import no.nav.dagpenger.vedtak.modell.entitet.Beløp.Companion.beløp
import no.nav.dagpenger.vedtak.modell.entitet.Stønadsdager
import no.nav.dagpenger.vedtak.modell.entitet.Timer
import no.nav.dagpenger.vedtak.modell.entitet.Timer.Companion.timer
import no.nav.dagpenger.vedtak.modell.utbetaling.Betalingsdag.Companion.summer
import no.nav.dagpenger.vedtak.modell.vedtak.ForbrukHistorikk
import no.nav.dagpenger.vedtak.modell.vedtak.TrukketEgenandelHistorikk
import no.nav.dagpenger.vedtak.modell.vedtak.Vedtak
import java.math.BigDecimal
import java.util.UUID

internal class LøpendeBehandling(
    private val rapporteringsId: UUID,
    internal val satsHistorikk: TemporalCollection<BigDecimal>,
    internal val stønadsdagerHistorikk: TemporalCollection<Stønadsdager>,
    internal val dagpengerettighetHistorikk: TemporalCollection<Dagpengerettighet>,
    internal val vanligArbeidstidHistorikk: TemporalCollection<Timer>,
    internal val egenandelHistorikk: TemporalCollection<Beløp>,
    internal val forbrukHistorikk: ForbrukHistorikk,
    internal val trukketEgenandelHistorikk: TrukketEgenandelHistorikk,

) {
    private val beregningsgrunnlag = Beregningsgrunnlag()

    fun håndter(rapporteringsperiode: Rapporteringsperiode): Vedtak {
        beregningsgrunnlag.populer(rapporteringsperiode, this)
        val sisteRapporteringdato = rapporteringsperiode.maxOf { it.dato() }
        val forrigeRapporteringsdato = rapporteringsperiode.minOf { it.dato() }.minusDays(1)
        val vilkårOppfylt = TaptArbeidstid().håndter(beregningsgrunnlag)

        val initieltForbruk = forbrukHistorikk.summer(forrigeRapporteringsdato)
        val stønadsperiode = stønadsdagerHistorikk.get(sisteRapporteringdato)

        val arbeidsdagerMedForbruk = when {
            vilkårOppfylt -> {
                val gjenståendeStønadsperiode = stønadsperiode - initieltForbruk
                Forbruk().håndter(beregningsgrunnlag, gjenståendeStønadsperiode)
            }

            else -> emptyList()
        }

        val utbetalingsdager =
            arbeidsdagerMedForbruk.map { it.tilBetalingsdag() } + beregningsgrunnlag.helgedagerMedRettighet()
                .filter { it.dag.arbeidstimer() > 0.timer }.map { it.tilBetalingsdag() }

        val initieltTrukketEgenandel = trukketEgenandelHistorikk.summer(forrigeRapporteringsdato)
        val originalSum = utbetalingsdager.summer()
        val egenandel = egenandelHistorikk.get(sisteRapporteringdato)
        val gjenståendeEgenandel = egenandel - initieltTrukketEgenandel

        val trukketEgenandel = if (gjenståendeEgenandel > 0.beløp && originalSum > 0.beløp) {
            minOf(gjenståendeEgenandel, originalSum)
        } else {
            0.beløp
        }

        val nySum = originalSum - trukketEgenandel

        return Vedtak.løpendeVedtak(
            behandlingId = UUID.randomUUID(),
            utfall = vilkårOppfylt,
            virkningsdato = sisteRapporteringdato,
            forbruk = Stønadsdager(arbeidsdagerMedForbruk.size),
            beløpTilUtbetaling = nySum,
            trukketEgenandel = trukketEgenandel,
        )
    }

    private fun sisteRettighetsdag() = beregningsgrunnlag.rettighetsdager().last().dag.dato()
}
