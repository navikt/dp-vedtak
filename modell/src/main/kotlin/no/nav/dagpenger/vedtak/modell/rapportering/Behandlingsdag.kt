package no.nav.dagpenger.vedtak.modell.rapportering

import no.nav.dagpenger.vedtak.modell.entitet.Beløp
import no.nav.dagpenger.vedtak.modell.entitet.Beløp.Companion.beløp
import no.nav.dagpenger.vedtak.modell.entitet.Prosent
import no.nav.dagpenger.vedtak.modell.entitet.Timer
import no.nav.dagpenger.vedtak.modell.entitet.Timer.Companion.summer
import no.nav.dagpenger.vedtak.modell.utbetaling.Utbetalingsdag
import no.nav.dagpenger.vedtak.modell.vedtak.rettighet.Hovedrettighet
import no.nav.dagpenger.vedtak.modell.vedtak.rettighet.IngenRettighet

class Behandlingsdag(
    internal val rapporteringsdag: Rapporteringsdag,
    internal val hovedrettighet: Hovedrettighet,
    private val vanligArbeidstidPerDag: Timer,
    private val dagsats: Beløp,
) {
    private var utbetaling: Beløp = 0.beløp

    internal fun terskel() = TaptArbeidstidTerskel.terskelFor(hovedrettighet.type, rapporteringsdag.dato())

    private fun dagpengerRettighetsdag(): Boolean = hovedrettighet != IngenRettighet && hovedrettighet.utfall

    internal fun utbetalingsdag(prosentFaktor: Prosent): Utbetalingsdag {
        this.utbetaling = prosentFaktor * dagsats
        return Utbetalingsdag(rapporteringsdag.dato(), utbetaling)
    }

    override fun toString(): String {
        return "Behandlingdag(rapporteringsdag=$rapporteringsdag, " +
            "hovedrettighet=${hovedrettighet.javaClass.simpleName}, " +
            "vanligArbeidstidPerDag=$vanligArbeidstidPerDag, " +
            "dagsats=$dagsats, utbetaling=$utbetaling)"
    }

    internal companion object {
        private fun Collection<Behandlingsdag>.rettighetsdagerUtenFravær() =
            this.filter { it.dagpengerRettighetsdag() }.filterNot { it.rapporteringsdag.fravær() }

        fun Collection<Behandlingsdag>.tellendeRapporteringsdager() =
            this.rettighetsdagerUtenFravær().filterNot { it.rapporteringsdag.erHelg() }

        fun Collection<Behandlingsdag>.vanligArbeidstid() = this.tellendeRapporteringsdager().map { it.vanligArbeidstidPerDag }.summer()

        fun Collection<Behandlingsdag>.arbeidedeTimer() =
            this.rettighetsdagerUtenFravær().map { it.rapporteringsdag.arbeidstimer() }.summer()

        private fun Collection<Behandlingsdag>.taptArbeidstid() = (this.vanligArbeidstid() - this.arbeidedeTimer())

        fun Collection<Behandlingsdag>.graderingsProsent() = this.taptArbeidstid() prosentAv this.vanligArbeidstid()
    }
}
