package no.nav.dagpenger.vedtak.modell.rapportering

import no.nav.dagpenger.vedtak.modell.entitet.Stønadsdager

internal class Forbruk : Regel {

    fun håndter(
        beregningsgrunnlag: Beregningsgrunnlag,
        gjenståendeStønadsdager: Stønadsdager,
    ): List<Beregningsgrunnlag.DagGrunnlag> {
        val arbeidsdagerMedRettighet = beregningsgrunnlag.arbeidsdagerMedRettighet()
        val antallArbeidsdagerMedRettighet = Stønadsdager(dager = arbeidsdagerMedRettighet.size)
        return if (antallArbeidsdagerMedRettighet > gjenståendeStønadsdager) {
            arbeidsdagerMedRettighet.subList(0, gjenståendeStønadsdager.stønadsdager())
        } else {
            arbeidsdagerMedRettighet
        }
    }
}
