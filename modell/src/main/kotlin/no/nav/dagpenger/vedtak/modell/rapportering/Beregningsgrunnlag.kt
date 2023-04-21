package no.nav.dagpenger.vedtak.modell.rapportering

import no.nav.dagpenger.vedtak.modell.Beløp
import no.nav.dagpenger.vedtak.modell.Beløp.Companion.beløp
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
                    sats = kotlin.runCatching { løpendeBehandling.satsHistorikk.get(dag.dato()) }
                        .getOrDefault(0.toBigDecimal()),
                    dagpengerettighet = kotlin.runCatching { løpendeBehandling.dagpengerettighetHistorikk.get(dag.dato()) }
                        .getOrDefault(Dagpengerettighet.Ingen),
                    vanligArbeidstid = kotlin.runCatching { løpendeBehandling.vanligArbeidstidHistorikk.get(dag.dato()) }
                        .getOrDefault(0.timer),
                ),
            )
        }
    }

    fun rettighetsdager(): List<DagGrunnlag> = fakta.filter(rettighetsdag())
    fun arbeidsdagerMedRettighet(): List<DagGrunnlag> = fakta.filter(rettighetsdag()).filter(arbeidsdag())

    fun helgedagerMedRettighet(): List<DagGrunnlag> = fakta.filter(rettighetsdag()).filter(helgedag())

    private fun rettighetsdag(): (DagGrunnlag) -> Boolean = { it is Rettighetsdag }
    private fun arbeidsdag() = { it: DagGrunnlag -> it.dag is Arbeidsdag }
    private fun helgedag() = { it: DagGrunnlag -> it.dag is Helgedag }

    internal sealed class DagGrunnlag(internal val dag: Dag) {
        abstract fun sats(): BigDecimal
        abstract fun dagpengerettighet(): Dagpengerettighet
        abstract fun vanligArbeidstid(): Timer
        abstract fun terskelTaptArbeidstid(): Prosent

        // abstract fun egenandel(gjenståendeEgenandelHistorikk: TemporalCollection<BigDecimal>)
        abstract fun egenandel(gjenståendeEgenandelHistorikk: TemporalCollection<BigDecimal>): BigDecimal
        abstract fun tilBetalingsdag(): Betalingsdag

        companion object {
            fun opprett(
                dag: Dag,
                dagpengerettighet: Dagpengerettighet,
                sats: BigDecimal,
                vanligArbeidstid: Timer,
            ): DagGrunnlag {
                return when (dagpengerettighet) {
                    Dagpengerettighet.Ingen -> IngenRettighetsdag(dag, dagpengerettighet)
                    else -> Rettighetsdag(dag, dagpengerettighet, sats, vanligArbeidstid, 0.timer)
                }
            }
        }
    }

    internal class IngenRettighetsdag(dag: Dag, private val dagpengerettighet: Dagpengerettighet) : DagGrunnlag(dag) {

        init {
            require(dagpengerettighet == Dagpengerettighet.Ingen) { "Støtter bare Dagpengerettighet.Ingen" }
        }

        override fun sats(): BigDecimal =
            throw IllegalArgumentException("Dag ${dag.dato()} har ingen rettighet og har derfor ikke sats")

        override fun dagpengerettighet(): Dagpengerettighet = dagpengerettighet

        override fun vanligArbeidstid(): Timer =
            throw IllegalArgumentException("Dag ${dag.dato()} har ingen rettighet og har derfor ikke vanlig arbeidstid")

        override fun terskelTaptArbeidstid(): Prosent =
            throw IllegalArgumentException("Dag ${dag.dato()} har ingen rettighet og har derfor ikke terskel for tapt arbeidstid")

//        override fun egenandel(gjenståendeEgenandelHistorikk: TemporalCollection<BigDecimal>) {}
        override fun egenandel(gjenståendeEgenandelHistorikk: TemporalCollection<BigDecimal>): BigDecimal =
            throw IllegalArgumentException("Dag ${dag.dato()} har ingen rettighet og har derfor ikke egenandel")

        override fun tilBetalingsdag(): Betalingsdag = IkkeUtbetalingsdag(dag.dato())
    }

    internal class Rettighetsdag(
        dag: Dag,
        private val dagpengerettighet: Dagpengerettighet,
        private val sats: BigDecimal,
        private val vanligArbeidstid: Timer,
        private var egenandelAsTimer: Timer,
    ) : DagGrunnlag(dag) {
        override fun sats(): BigDecimal = sats
        override fun dagpengerettighet(): Dagpengerettighet = dagpengerettighet
        override fun vanligArbeidstid(): Timer = vanligArbeidstid
        override fun terskelTaptArbeidstid(): Prosent = TaptArbeidstid.Terskel.terskelFor(dagpengerettighet, dag.dato())
        override fun egenandel(gjenståendeEgenandelHistorikk: TemporalCollection<BigDecimal>): BigDecimal {
            val gjenståendeEgenandel = gjenståendeEgenandelHistorikk.get(dag.dato())
            var egenandel = BigDecimal(0)
            if (gjenståendeEgenandel > BigDecimal(0)) {
                val taptArbeidstid = vanligArbeidstid - dag.arbeidstimer()
                val dagpengerForTaptArbeidstid =
                    sats.div(vanligArbeidstid.timer.toBigDecimal()) * taptArbeidstid.timer.toBigDecimal()
                if (dagpengerForTaptArbeidstid != BigDecimal(0)) {
                    egenandel = gjenståendeEgenandel.min(dagpengerForTaptArbeidstid)
                    //gjenståendeEgenandelHistorikk.put(dag.dato(), gjenståendeEgenandel - egenandel)
                }
            }
            if (egenandel != BigDecimal(0)) {
                egenandelAsTimer = Timer(sats.div(egenandel)) * vanligArbeidstid.timer
            }

            return egenandel
        }
//        override fun egenandel(gjenståendeEgenandelHistorikk: TemporalCollection<BigDecimal>) {
//            val gjenståendeEgenandel = gjenståendeEgenandelHistorikk.get(dag.dato())
//            if (gjenståendeEgenandel > BigDecimal(0)) {
//                val taptArbeidstid = vanligArbeidstid - dag.arbeidstimer()
//                val dagpengerForTaptArbeidstid = sats.div(vanligArbeidstid.timer.toBigDecimal()) * taptArbeidstid.timer.toBigDecimal()
//                if (dagpengerForTaptArbeidstid != BigDecimal(0)) {
//
//                    this.egenandel = gjenståendeEgenandel.min(dagpengerForTaptArbeidstid)
//                }
//                if (dagpengerForTaptArbeidstid > gjenståendeEgenandel) {
//                    gjenståendeEgenandelHistorikk.put(dag.dato(), BigDecimal(0))
//                    this.egenandel = BigDecimal(0)
//                } else if (dagpengerForTaptArbeidstid != BigDecimal(0)) {
//                    gjenståendeEgenandelHistorikk.put(dag.dato(), gjenståendeEgenandel - dagpengerForTaptArbeidstid)
//                }
//            }
//        }

        override fun tilBetalingsdag(): Betalingsdag {
            // val utbetalingstimer = (if (dag is Helgedag) 0.timer else vanligArbeidstid) - dag.arbeidstimer()
            val utbetalingstimer = utbetalingstimer()
            val timeSats = timeSats()
            val beløp = timeSats * utbetalingstimer
            return Utbetalingsdag(dag.dato(), beløp)
        }
        private fun utbetalingstimer(): Timer {
            if (dag is Helgedag) {
                return 0.timer - dag.arbeidstimer()
            } else if (egenandelAsTimer != 0.timer) {
                return vanligArbeidstid - egenandelAsTimer - dag.arbeidstimer()
            } else {
                return vanligArbeidstid - dag.arbeidstimer()
            }
        }
        private fun timeSats(): Beløp {
            return when (dag) {
                is Arbeidsdag, is Helgedag -> Beløp.fra(sats) / vanligArbeidstid
                is Fraværsdag -> 0.beløp
            }
        }
    }
}
