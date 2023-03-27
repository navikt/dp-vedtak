package no.nav.dagpenger.vedtak.modell.rapportering

import no.nav.dagpenger.vedtak.modell.Dagpengerettighet
import no.nav.dagpenger.vedtak.modell.entitet.Timer
import no.nav.dagpenger.vedtak.modell.entitet.Timer.Companion.timer
import java.math.BigDecimal

internal class Beregningsgrunnlag(private val fakta: MutableList<Faktum> = mutableListOf()) {

    fun populer(rapporteringsperiode: Rapporteringsperiode, løpendeBehandling: LøpendeBehandling) {
        rapporteringsperiode.map { dag ->
            fakta.add(
                Faktum(
                    dag = dag,
                    sats = kotlin.runCatching { løpendeBehandling.satshistorikk.get(dag.dato) }
                        .getOrDefault(0.toBigDecimal()),
                    rettighet = kotlin.runCatching { løpendeBehandling.rettighethistorikk.get(dag.dato) }
                        .getOrDefault(Dagpengerettighet.Ingen),
                    vanligarbeidstid = kotlin.runCatching { løpendeBehandling.vanligarbeidstidhistorikk.get(dag.dato) }
                        .getOrDefault(0.timer),
                ),
            )
        }
    }

    fun rettighetsdager(): List<Faktum> = fakta.filter(rettighetsdag())
    fun arbeidsdagerMedRettighet(): List<Faktum> = fakta.filter(rettighetsdag()).filter(arbeidsdag())

    private fun rettighetsdag(): (Faktum) -> Boolean = { it.rettighet != Dagpengerettighet.Ingen }
    private fun arbeidsdag() = { it: Faktum -> it.dag is Arbeidsdag }

    internal data class Faktum(
        val dag: Dag,
        val sats: BigDecimal,
        val rettighet: Dagpengerettighet,
        val vanligarbeidstid: Timer,
    ) {
        fun terskel() = TaptArbeidstid.Terskel.terskelFor(rettighet, dag.dato)
    }
}
