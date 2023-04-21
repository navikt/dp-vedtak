package no.nav.dagpenger.vedtak.modell.rapportering

import no.nav.dagpenger.vedtak.modell.TemporalCollection
import java.math.BigDecimal

internal class Forbruk : Regel {

    fun håndter(beregningsgrunnlag: Beregningsgrunnlag): List<Beregningsgrunnlag.DagGrunnlag> {
        return beregningsgrunnlag.arbeidsdagerMedRettighet()
    }
}

internal class Egenandel : Regel {
    fun håndter(beregningsgrunnlag: List<Beregningsgrunnlag.DagGrunnlag>, gjenståendeEgenandelHistorikk: TemporalCollection<BigDecimal>) {
        beregningsgrunnlag.forEach { it.egenandel(gjenståendeEgenandelHistorikk) }
    }
}
