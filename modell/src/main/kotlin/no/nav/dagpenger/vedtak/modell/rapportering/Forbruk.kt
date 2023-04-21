package no.nav.dagpenger.vedtak.modell.rapportering

internal class Forbruk : Regel {

    fun håndter(beregningsgrunnlag: Beregningsgrunnlag): List<Beregningsgrunnlag.DagGrunnlag> {
        return beregningsgrunnlag.arbeidsdagerMedRettighet()
    }
}
