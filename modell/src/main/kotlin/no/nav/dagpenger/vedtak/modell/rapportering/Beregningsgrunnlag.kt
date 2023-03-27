package no.nav.dagpenger.vedtak.modell.rapportering

import no.nav.dagpenger.vedtak.modell.Dagpengerettighet
import no.nav.dagpenger.vedtak.modell.entitet.Prosent
import no.nav.dagpenger.vedtak.modell.entitet.Timer
import no.nav.dagpenger.vedtak.modell.entitet.Timer.Companion.timer
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

    internal sealed class DagGrunnlag(internal val dag: Dag) {
        abstract fun sats(): BigDecimal
        abstract fun rettighet(): Dagpengerettighet
        abstract fun vanligArbeidstid(): Timer
        abstract fun terskel(): Prosent

        abstract fun ventetidTimer(): Timer

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

        override fun ventetidTimer(): Timer = 0.timer
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
        override fun ventetidTimer(): Timer {
            return when (dag) {
                is Arbeidsdag -> vanligarbeidstid - dag.arbeidstimer()
                is Fraværsdag -> 0.timer
                is Helgedag -> Timer(dag.arbeidstimer().negate())
            }
        }
    }
}
