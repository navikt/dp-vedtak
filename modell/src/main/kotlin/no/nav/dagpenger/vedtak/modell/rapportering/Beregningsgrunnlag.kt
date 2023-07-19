package no.nav.dagpenger.vedtak.modell.rapportering

import no.nav.dagpenger.vedtak.modell.Dagpengerettighet
import no.nav.dagpenger.vedtak.modell.entitet.Beløp
import no.nav.dagpenger.vedtak.modell.entitet.Beløp.Companion.beløp
import no.nav.dagpenger.vedtak.modell.entitet.Prosent
import no.nav.dagpenger.vedtak.modell.entitet.Timer
import no.nav.dagpenger.vedtak.modell.entitet.Timer.Companion.summer
import no.nav.dagpenger.vedtak.modell.entitet.Timer.Companion.timer
import no.nav.dagpenger.vedtak.modell.utbetaling.BeregnetBeløpDag
import no.nav.dagpenger.vedtak.modell.utbetaling.LøpendeRettighetDag
import no.nav.dagpenger.vedtak.modell.utbetaling.NullBeløpDag

internal class Beregningsgrunnlag(private val fakta: MutableList<DagGrunnlag> = mutableListOf()) {

    fun populer(rapporteringsperiode: Rapporteringsperiode, løpendeBehandling: LøpendeBehandling) {
        rapporteringsperiode.map { dag ->
            fakta.add(
                DagGrunnlag.opprett(
                    dag = dag,
                    sats = kotlin.runCatching { løpendeBehandling.satsHistorikk.get(dag.dato()) }
                        .getOrDefault(0.beløp),
                    dagpengerettighet = kotlin.runCatching { løpendeBehandling.dagpengerettighetHistorikk.get(dag.dato()) }
                        .getOrDefault(Dagpengerettighet.Ingen),
                    vanligArbeidstid = kotlin.runCatching { løpendeBehandling.vanligArbeidstidHistorikk.get(dag.dato()) }
                        .getOrDefault(0.timer),
                ),
            )
        }
    }

    fun rettighetsdager(): List<DagGrunnlag> = fakta.filter(rettighetsdag())
    fun arbeidsdagerMedRettighet(): List<DagGrunnlag> = fakta.filter(rettighetsdag()).filterNot(helgedag())
    internal fun vanligArbeidstid() = arbeidsdagerMedRettighet().map { it.vanligArbeidstid() }.summer()
    internal fun arbeidedeTimer() = rettighetsdager().map { it.dag.arbeidstimer() }.summer()
    private fun taptArbeidstid() = (vanligArbeidstid() - arbeidedeTimer())
    fun graderingsProsent() = taptArbeidstid() prosentAv vanligArbeidstid()

    private fun rettighetsdag(): (DagGrunnlag) -> Boolean = { it is Rettighetsdag && !it.dag.fravær() }
    private fun helgedag() = { it: DagGrunnlag -> it.dag.erHelg() }

    internal sealed class DagGrunnlag(internal val dag: Dag) {
        abstract fun sats(): Beløp
        abstract fun dagpengerettighet(): Dagpengerettighet
        abstract fun vanligArbeidstid(): Timer
        abstract fun terskelTaptArbeidstid(): Prosent
        abstract fun tilLøpendeRettighetDag(graderingsProsent: Prosent): LøpendeRettighetDag

        companion object {
            fun opprett(
                dag: Dag,
                dagpengerettighet: Dagpengerettighet,
                sats: Beløp,
                vanligArbeidstid: Timer,
            ): DagGrunnlag {
                return when (dagpengerettighet) {
                    Dagpengerettighet.Ingen -> IngenRettighetsdag(dag, dagpengerettighet)
                    else -> Rettighetsdag(dag, dagpengerettighet, sats, vanligArbeidstid)
                }
            }
        }
    }

    internal class IngenRettighetsdag(dag: Dag, private val dagpengerettighet: Dagpengerettighet) : DagGrunnlag(dag) {

        init {
            require(dagpengerettighet == Dagpengerettighet.Ingen) { "Støtter bare Dagpengerettighet.Ingen" }
        }

        override fun sats(): Beløp =
            throw IllegalArgumentException("Dag ${dag.dato()} har ingen rettighet og har derfor ikke sats")

        override fun dagpengerettighet(): Dagpengerettighet = dagpengerettighet

        override fun vanligArbeidstid(): Timer =
            throw IllegalArgumentException("Dag ${dag.dato()} har ingen rettighet og har derfor ikke vanlig arbeidstid")

        override fun terskelTaptArbeidstid(): Prosent =
            throw IllegalArgumentException("Dag ${dag.dato()} har ingen rettighet og har derfor ikke terskel for tapt arbeidstid")

        override fun tilLøpendeRettighetDag(graderingsProsent: Prosent): LøpendeRettighetDag = NullBeløpDag(dag.dato())
    }

    internal class Rettighetsdag(
        dag: Dag,
        private val dagpengerettighet: Dagpengerettighet,
        private val sats: Beløp,
        private val vanligArbeidstid: Timer,
    ) : DagGrunnlag(dag) {

        private val terskelProsent = TaptArbeidstid.Terskel.terskelFor(dagpengerettighet, dag.dato())
        override fun sats(): Beløp = sats
        override fun dagpengerettighet(): Dagpengerettighet = dagpengerettighet
        override fun vanligArbeidstid(): Timer = vanligArbeidstid
        override fun terskelTaptArbeidstid(): Prosent = terskelProsent
        override fun tilLøpendeRettighetDag(graderingsProsent: Prosent): LøpendeRettighetDag {
            val beløp = graderingsProsent * sats()
            return BeregnetBeløpDag(dag.dato(), beløp)
        }
    }
}
