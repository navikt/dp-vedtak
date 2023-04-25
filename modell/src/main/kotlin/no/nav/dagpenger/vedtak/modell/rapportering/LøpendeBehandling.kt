package no.nav.dagpenger.vedtak.modell.rapportering

import no.nav.dagpenger.vedtak.modell.Dagpengerettighet
import no.nav.dagpenger.vedtak.modell.TemporalCollection
import no.nav.dagpenger.vedtak.modell.entitet.Beløp
import no.nav.dagpenger.vedtak.modell.entitet.Beløp.Companion.beløp
import no.nav.dagpenger.vedtak.modell.entitet.Timer
import no.nav.dagpenger.vedtak.modell.entitet.Timer.Companion.timer
import no.nav.dagpenger.vedtak.modell.mengde.Enhet.Companion.arbeidsdager
import no.nav.dagpenger.vedtak.modell.utbetaling.Betalingsdag.Companion.summer
import no.nav.dagpenger.vedtak.modell.vedtak.Vedtak
import java.math.BigDecimal
import java.util.UUID

internal class LøpendeBehandling(
    private val rapporteringsId: UUID,
    internal val satsHistorikk: TemporalCollection<BigDecimal>,
    internal val dagpengerettighetHistorikk: TemporalCollection<Dagpengerettighet>,
    internal val vanligArbeidstidHistorikk: TemporalCollection<Timer>,
    internal val gjenståendeEgenandelHistorikk: TemporalCollection<Beløp>,

) {
    private val beregningsgrunnlag = Beregningsgrunnlag()

    fun håndter(rapporteringsperiode: Rapporteringsperiode): Vedtak {
        beregningsgrunnlag.populer(rapporteringsperiode, this)
        val vilkårOppfylt = TaptArbeidstid().håndter(beregningsgrunnlag)

        val arbeidsdagerMedForbruk = when {
            vilkårOppfylt -> Forbruk().håndter(beregningsgrunnlag)
            else -> emptyList()
        }

        val forbruk = arbeidsdagerMedForbruk.size.arbeidsdager
        val utbetalingsdager = arbeidsdagerMedForbruk.map { it.tilBetalingsdag() } + beregningsgrunnlag.helgedagerMedRettighet().filter { it.dag.arbeidstimer() > 0.timer }.map { it.tilBetalingsdag() }

        val gjenståendeEgenandel = gjenståendeEgenandelHistorikk.get(førsteRettighetsdag())
        val originalSum = utbetalingsdager.summer()

        val trukketEgenandel = if (gjenståendeEgenandel > 0.beløp && originalSum > 0.beløp) {
            minOf(gjenståendeEgenandel, originalSum)
        } else {
            0.beløp
        }

        val nySum = originalSum - trukketEgenandel

        return Vedtak.løpendeVedtak(
            behandlingId = UUID.randomUUID(),
            utfall = vilkårOppfylt,
            virkningsdato = førsteRettighetsdag(),
            forbruk = forbruk,
            beløpTilUtbetaling = nySum,
            trukketEgenandel = trukketEgenandel,
        )
    }

    private fun førsteRettighetsdag() = beregningsgrunnlag.rettighetsdager().first().dag.dato()
}
