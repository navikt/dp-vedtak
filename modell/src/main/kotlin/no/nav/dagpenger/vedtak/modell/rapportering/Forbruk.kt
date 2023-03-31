package no.nav.dagpenger.vedtak.modell.rapportering

import no.nav.dagpenger.vedtak.modell.TemporalCollection
import no.nav.dagpenger.vedtak.modell.entitet.Timer

internal class Forbruk : Regel {

    fun håndter(beregningsgrunnlag: Beregningsgrunnlag, ventetidhistorikk: TemporalCollection<Timer>): List<Beregningsgrunnlag.DagGrunnlag> {
        beregningsgrunnlag.arbeidsdagerMedRettighet().map { it.ventetidTimer(ventetidhistorikk) }

        // ventetid forbruk
        //
        return beregningsgrunnlag.arbeidsdagerMedRettighet().filterNot { it.ventedag }
    }
}
