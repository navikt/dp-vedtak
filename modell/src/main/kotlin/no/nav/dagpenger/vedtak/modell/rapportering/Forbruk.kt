package no.nav.dagpenger.vedtak.modell.rapportering

import no.nav.dagpenger.vedtak.modell.mengde.Enhet.Companion.arbeidsdager
import no.nav.dagpenger.vedtak.modell.mengde.RatioMengde

internal class Forbruk : Regel {

    fun håndter(beregningsgrunnlag: Beregningsgrunnlag, gjenståendeStønadsperiode: RatioMengde): List<Beregningsgrunnlag.DagGrunnlag> {
        val arbeidsdagerMedRettighet = beregningsgrunnlag.arbeidsdagerMedRettighet()
        val antallArbeidsdagerMedRettighet = arbeidsdagerMedRettighet.size.arbeidsdager
        return if (antallArbeidsdagerMedRettighet > gjenståendeStønadsperiode) {
            arbeidsdagerMedRettighet.subList(0, gjenståendeStønadsperiode.reflection { it.toInt() } - 1)
        } else {
            arbeidsdagerMedRettighet
        }
    }
}
