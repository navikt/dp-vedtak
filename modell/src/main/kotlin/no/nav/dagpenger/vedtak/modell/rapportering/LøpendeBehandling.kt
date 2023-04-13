package no.nav.dagpenger.vedtak.modell.rapportering

import no.nav.dagpenger.vedtak.modell.Dagpengerettighet
import no.nav.dagpenger.vedtak.modell.TemporalCollection
import no.nav.dagpenger.vedtak.modell.entitet.Timer
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
    internal val gjenståendeVentetidHistorikk: TemporalCollection<Timer>,

) {
    private val beregningsgrunnlag = Beregningsgrunnlag()

    fun håndter(rapporteringsperiode: Rapporteringsperiode): Vedtak {
        beregningsgrunnlag.populer(rapporteringsperiode, this)
        val vilkårOppfylt = TaptArbeidstid().håndter(beregningsgrunnlag)

        val dagerMedForbruk = when {
            vilkårOppfylt -> Forbruk().håndter(beregningsgrunnlag, gjenståendeVentetidHistorikk)
            else -> emptyList()
        }

        val forbruk = dagerMedForbruk.size.arbeidsdager
        val utbetalingsdager = dagerMedForbruk.map { it.tilBetalingsdag() }

        return Vedtak.løpendeVedtak(
            behandlingId = UUID.randomUUID(),
            utfall = vilkårOppfylt,
            virkningsdato = førsteRettighetsdag(),
            forbruk = forbruk,
            beløpTilUtbetaling = utbetalingsdager.summer(),
        )
    }

    private fun førsteRettighetsdag() = beregningsgrunnlag.rettighetsdager().first().dag.dato()
}
