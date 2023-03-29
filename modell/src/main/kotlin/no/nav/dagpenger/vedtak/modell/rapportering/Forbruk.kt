package no.nav.dagpenger.vedtak.modell.rapportering

import no.nav.dagpenger.vedtak.modell.TemporalCollection
import no.nav.dagpenger.vedtak.modell.entitet.Timer
import no.nav.dagpenger.vedtak.modell.mengde.Enhet.Companion.arbeidsdager
import no.nav.dagpenger.vedtak.modell.mengde.Tid

internal class Forbruk : Regel {

    fun håndtere(beregningsgrunnlag: Beregningsgrunnlag, ventetidhistorikk: TemporalCollection<Timer>): Tid {
        beregningsgrunnlag.arbeidsdagerMedRettighet().map { it.ventetidTimer(ventetidhistorikk) }

        // ventetid forbruk
        //
        return beregningsgrunnlag.arbeidsdagerMedRettighet().filterNot { it.ventedag }.size.arbeidsdager
    }
}
