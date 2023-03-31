package no.nav.dagpenger.vedtak.modell.rapportering

import no.nav.dagpenger.vedtak.modell.Beløp
import no.nav.dagpenger.vedtak.modell.Dagpengerettighet
import no.nav.dagpenger.vedtak.modell.TemporalCollection
import no.nav.dagpenger.vedtak.modell.entitet.Prosent
import no.nav.dagpenger.vedtak.modell.entitet.Timer
import no.nav.dagpenger.vedtak.modell.entitet.Timer.Companion.timer
import no.nav.dagpenger.vedtak.modell.utbetaling.Betalingsdag
import no.nav.dagpenger.vedtak.modell.utbetaling.IkkeUtbetalingsdag
import no.nav.dagpenger.vedtak.modell.utbetaling.Utbetalingsdag
import java.math.BigDecimal

internal class Beregningsgrunnlag(private val fakta: MutableList<DagGrunnlag> = mutableListOf()) {

    fun populer(rapporteringsperiode: Rapporteringsperiode, løpendeBehandling: LøpendeBehandling) {
        rapporteringsperiode.map { dag ->
            fakta.add(
                DagGrunnlag.opprett(
                    dag = dag,
                    sats = kotlin.runCatching { løpendeBehandling.satshistorikk.get(dag.dato()) }
                        .getOrDefault(0.toBigDecimal()),
                    dagpengerettighet = kotlin.runCatching { løpendeBehandling.rettighethistorikk.get(dag.dato()) }
                        .getOrDefault(Dagpengerettighet.Ingen),
                    vanligarbeidstid = kotlin.runCatching { løpendeBehandling.vanligarbeidstidhistorikk.get(dag.dato()) }
                        .getOrDefault(0.timer),
                ),
            )
        }
    }

    fun rettighetsdager(): List<DagGrunnlag> = fakta.filter(rettighetsdag())
    fun arbeidsdagerMedRettighet(): List<DagGrunnlag> = fakta.filter(rettighetsdag()).filter(arbeidsdag())

    private fun rettighetsdag(): (DagGrunnlag) -> Boolean = { it is Rettighetsdag }
    private fun arbeidsdag() = { it: DagGrunnlag -> it.dag is Arbeidsdag }

    internal sealed class DagGrunnlag(internal val dag: Dag, internal var ventedag: Boolean = false, var gjenståendeTaptArbeidstid: Timer = dag.arbeidstimer()) {
        abstract fun sats(): BigDecimal
        abstract fun rettighet(): Dagpengerettighet
        abstract fun vanligArbeidstid(): Timer
        abstract fun terskel(): Prosent
        abstract fun ventetidTimer(ventetidhistorikk: TemporalCollection<Timer>)

        abstract fun tilBetalingsdag(): Betalingsdag

        companion object {
            fun opprett(
                dag: Dag,
                dagpengerettighet: Dagpengerettighet,
                sats: BigDecimal,
                vanligarbeidstid: Timer,
            ): DagGrunnlag {
                return when (dagpengerettighet) {
                    Dagpengerettighet.Ingen -> IngenRettighetsdag(dag, dagpengerettighet)
                    else -> Rettighetsdag(dag, dagpengerettighet, sats, vanligarbeidstid)
                }
            }
        }
    }

    internal class IngenRettighetsdag(dag: Dag, private val dagpengerettighet: Dagpengerettighet) : DagGrunnlag(dag) {

        init {
            require(dagpengerettighet == Dagpengerettighet.Ingen) { "Støtter bare Dagpengerettighet.Ingen" }
        }

        override fun sats(): BigDecimal =
            throw IllegalArgumentException("Dag ${dag.dato()} har ingen rettighet og har ikke sats")

        override fun rettighet(): Dagpengerettighet = dagpengerettighet

        override fun vanligArbeidstid(): Timer =
            throw IllegalArgumentException("Dag ${dag.dato()} har ingen rettighet og har ikke vanligarbeidstid")

        override fun terskel(): Prosent =
            throw IllegalArgumentException("Dag ${dag.dato()} har ingen rettighet og har ikke terskel")

        override fun ventetidTimer(ventetidhistorikk: TemporalCollection<Timer>) {}
        override fun tilBetalingsdag(): Betalingsdag = IkkeUtbetalingsdag(dag.dato())
    }

    internal class Rettighetsdag(
        dag: Dag,
        private val dagpengerettighet: Dagpengerettighet,
        private val sats: BigDecimal,
        private val vanligarbeidstid: Timer,
    ) : DagGrunnlag(dag) {
        override fun sats(): BigDecimal = sats
        override fun rettighet(): Dagpengerettighet = dagpengerettighet
        override fun vanligArbeidstid(): Timer = vanligarbeidstid
        override fun terskel(): Prosent = TaptArbeidstid.Terskel.terskelFor(dagpengerettighet, dag.dato())
        override fun ventetidTimer(ventetidhistorikk: TemporalCollection<Timer>) {
            val gjenståendeVentetid = ventetidhistorikk.get(dag.dato())
            if (gjenståendeVentetid > 0.timer) {
                this.ventedag = true
                this.gjenståendeTaptArbeidstid = vanligarbeidstid - dag.arbeidstimer()
                val nyGjenstående = gjenståendeVentetid - gjenståendeTaptArbeidstid
                if (nyGjenstående > 0.timer) {
                    ventetidhistorikk.put(dag.dato(), nyGjenstående)
                } else {
                    ventetidhistorikk.put(dag.dato(), 0.timer)
                }
            }
        }

        override fun tilBetalingsdag(): Betalingsdag = Utbetalingsdag(dag.dato(), Beløp.fra(sats))
    }
}
