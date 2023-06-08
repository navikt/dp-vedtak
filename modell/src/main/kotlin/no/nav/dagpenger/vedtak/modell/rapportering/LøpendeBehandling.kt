package no.nav.dagpenger.vedtak.modell.rapportering

import no.nav.dagpenger.vedtak.modell.Dagpengerettighet
import no.nav.dagpenger.vedtak.modell.TemporalCollection
import no.nav.dagpenger.vedtak.modell.entitet.Beløp
import no.nav.dagpenger.vedtak.modell.entitet.Beløp.Companion.beløp
import no.nav.dagpenger.vedtak.modell.entitet.Stønadsdager
import no.nav.dagpenger.vedtak.modell.entitet.Timer
import no.nav.dagpenger.vedtak.modell.entitet.Timer.Companion.timer
import no.nav.dagpenger.vedtak.modell.hendelser.Rapporteringshendelse
import no.nav.dagpenger.vedtak.modell.utbetaling.LøpendeRettighetDag.Companion.summer
import no.nav.dagpenger.vedtak.modell.vedtak.ForbrukHistorikk
import no.nav.dagpenger.vedtak.modell.vedtak.LøpendeRettighetVedtak
import no.nav.dagpenger.vedtak.modell.vedtak.TrukketEgenandelHistorikk
import no.nav.dagpenger.vedtak.modell.vedtak.Vedtak
import java.math.BigDecimal
import java.time.LocalDate
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

    fun håndter(rapporteringshendelse: Rapporteringshendelse) {
        val somPeriode = rapporteringshendelse.somPeriode()
    }

    fun håndter(rapporteringsperiode: Rapporteringsperiode): Vedtak {
        beregningsgrunnlag.populer(rapporteringsperiode, this)
        val vilkårOppfylt = TaptArbeidstid().håndter(beregningsgrunnlag)

        return if (vilkårOppfylt) {
            løpendeRettighetVedtak(rapporteringsperiode)
        } else {
            Vedtak.løpendeRettighet(
                behandlingId = UUID.randomUUID(),
                utfall = false,
                virkningsdato = rapporteringsperiode.maxOf { it.dato() },
            )
        }
    }

    private fun løpendeRettighetVedtak(rapporteringsperiode: Rapporteringsperiode): LøpendeRettighetVedtak {
        val sisteRapporteringsdato = rapporteringsperiode.maxOf { it.dato() }
        val forrigeRapporteringsdato = rapporteringsperiode.minOf { it.dato() }.minusDays(1)
        val initieltForbruk = forbrukHistorikk.summer(forrigeRapporteringsdato)
        val stønadsdager = stønadsdagerHistorikk.get(sisteRapporteringsdato)

        val arbeidsdagerMedForbruk = arbeidsdagerMedForbruk(vilkårOppfylt = true, stønadsdager, initieltForbruk)
        val rettighetsdager =
            arbeidsdagerMedForbruk.map { it.tilLøpendeRettighetDag() } + beregningsgrunnlag.helgedagerMedRettighet()
                .filter { it.dag.arbeidstimer() > 0.timer }.map { it.tilLøpendeRettighetDag() }
        val beregnetBeløpFørTrekkAvEgenandel = rettighetsdager.summer()
        val trukketEgenandel = beregnEgenandel(forrigeRapporteringsdato, sisteRapporteringsdato, beregnetBeløpFørTrekkAvEgenandel)

        return Vedtak.løpendeRettighet(
            behandlingId = UUID.randomUUID(),
            utfall = true,
            virkningsdato = rapporteringsperiode.maxOf { it.dato() },
            forbruk = Stønadsdager(arbeidsdagerMedForbruk.size),
            rettighetsdager = rettighetsdager,
            trukketEgenandel = trukketEgenandel,
        )
    }

    private fun arbeidsdagerMedForbruk(
        vilkårOppfylt: Boolean,
        stønadsperiode: Stønadsdager,
        initieltForbruk: Stønadsdager,
    ) = when {
        vilkårOppfylt -> {
            val gjenståendeStønadsperiode = stønadsperiode - initieltForbruk
            Forbruk().håndter(beregningsgrunnlag, gjenståendeStønadsperiode)
        }

        else -> emptyList()
    }

    private fun beregnEgenandel(forrigeRapporteringsdato: LocalDate, sisteRapporteringsdato: LocalDate, beregnetBeløpFørTrekkAvEgenandel: Beløp): Beløp {
        val initieltTrukketEgenandel = trukketEgenandelHistorikk.summer(forrigeRapporteringsdato)
        val egenandel = egenandelHistorikk.get(sisteRapporteringsdato)
        val gjenståendeEgenandel = egenandel - initieltTrukketEgenandel

        return if (gjenståendeEgenandel > 0.beløp && beregnetBeløpFørTrekkAvEgenandel > 0.beløp) {
            minOf(gjenståendeEgenandel, beregnetBeløpFørTrekkAvEgenandel)
        } else {
            0.beløp
        }
    }
}
