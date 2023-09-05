package no.nav.dagpenger.vedtak.modell.rapportering

import no.nav.dagpenger.vedtak.modell.entitet.Beløp
import no.nav.dagpenger.vedtak.modell.entitet.Beløp.Companion.beløp
import no.nav.dagpenger.vedtak.modell.entitet.Prosent
import no.nav.dagpenger.vedtak.modell.entitet.Timer
import no.nav.dagpenger.vedtak.modell.entitet.Timer.Companion.summer
import no.nav.dagpenger.vedtak.modell.utbetaling.Utbetalingsdag
import no.nav.dagpenger.vedtak.modell.vedtak.rettighet.Hovedrettighet
import no.nav.dagpenger.vedtak.modell.vedtak.rettighet.IngenRettighet

class Behandlingdag(
    internal val dag: Dag,
    internal val hovedrettighet: Hovedrettighet,
    private val vanligArbeidstidPerDag: Timer,
    private val dagsats: Beløp,

) {

    private var utbetaling: Beløp = 0.beløp

    internal fun terskel() = TaptArbeidstidTerskel.terskelFor(hovedrettighet.type, dag.dato())
    private fun dagpengerRettighetsdag(): Boolean = hovedrettighet != IngenRettighet && hovedrettighet.utfall

    internal fun utbetalingsdag(prosentFaktor: Prosent): Utbetalingsdag {
        this.utbetaling = prosentFaktor * dagsats
        return Utbetalingsdag(dag.dato(), utbetaling)
    }

    internal companion object {
        private fun Collection<Behandlingdag>.rettighetsdagerUtenFravær() =
            this.filter { it.dagpengerRettighetsdag() }.filterNot { it.dag.fravær() }

        fun Collection<Behandlingdag>.tellendeRapporteringsdager() =
            this.rettighetsdagerUtenFravær().filterNot { it.dag.erHelg() }

        fun Collection<Behandlingdag>.vanligArbeidstid() =
            this.tellendeRapporteringsdager().map { it.vanligArbeidstidPerDag }.summer()

        fun Collection<Behandlingdag>.arbeidedeTimer() =
            this.rettighetsdagerUtenFravær().map { it.dag.arbeidstimer() }.summer()

        private fun Collection<Behandlingdag>.taptArbeidstid() = (this.vanligArbeidstid() - this.arbeidedeTimer())

        fun Collection<Behandlingdag>.graderingsProsent() = this.taptArbeidstid() prosentAv this.vanligArbeidstid()
    }
}
