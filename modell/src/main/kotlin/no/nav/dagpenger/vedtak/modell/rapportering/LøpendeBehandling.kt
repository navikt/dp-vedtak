package no.nav.dagpenger.vedtak.modell.rapportering

import no.nav.dagpenger.vedtak.modell.Dagpengerettighet
import no.nav.dagpenger.vedtak.modell.Dagpengerettighet.ForskutterteLønnsgarantimidler
import no.nav.dagpenger.vedtak.modell.Dagpengerettighet.Ingen
import no.nav.dagpenger.vedtak.modell.Dagpengerettighet.Ordinær
import no.nav.dagpenger.vedtak.modell.Dagpengerettighet.Permittering
import no.nav.dagpenger.vedtak.modell.Dagpengerettighet.PermitteringFraFiskeindustrien
import no.nav.dagpenger.vedtak.modell.TemporalCollection
import no.nav.dagpenger.vedtak.modell.entitet.Prosent
import no.nav.dagpenger.vedtak.modell.entitet.Prosent.Companion.summer
import no.nav.dagpenger.vedtak.modell.entitet.Timer
import no.nav.dagpenger.vedtak.modell.entitet.Timer.Companion.summer
import no.nav.dagpenger.vedtak.modell.entitet.Timer.Companion.timer
import no.nav.dagpenger.vedtak.modell.rapportering.Dag.Companion.summerArbeidstimer
import java.math.BigDecimal
import java.time.LocalDate
import java.util.UUID

internal class LøpendeBehandling(
    private val rapporteringsId: UUID,
    private val satshistorikk: TemporalCollection<BigDecimal>,
    private val rettighethistorikk: TemporalCollection<Dagpengerettighet>,
    private val vanligarbeidstidhistorikk: TemporalCollection<Timer>,

) {
    fun håndter(rapporteringsperiode: Rapporteringsperiode) {
        val fastsettelser = rapporteringsperiode.map { dag ->
            GjeldendeFastsettelser(
                dag = dag,
                sats = kotlin.runCatching { satshistorikk.get(dag.dato) }.getOrDefault(0.toBigDecimal()),
                rettighet = kotlin.runCatching { rettighethistorikk.get(dag.dato) }
                    .getOrDefault(Ingen),
                vanligarbeidstid = kotlin.runCatching { vanligarbeidstidhistorikk.get(dag.dato) }.getOrDefault(0.timer),
            )
        }

        val ok = TaptArbeidstid().håndter(fastsettelser)

        if (ok) {
            println("hurra")
        }
        println(fastsettelser)

// / hvilke dager? hvilke ikke dager?

        // tellendedager
//        if(vilkår.ok) {
//            fastsettelser.beregn()
//        }
        // returner vedtak
    }
}

interface Regel

internal class TaptArbeidstid : Regel {
    fun håndter(fastsettelser: List<GjeldendeFastsettelser>): Boolean {
        val tellendedager = fastsettelser.filterNot { it.rettighet == Ingen }
        val arbeidstimer: Timer = tellendedager.map { it.dag }.summerArbeidstimer()
        val arbeidsdager = tellendedager.filter { it.dag is Arbeidsdag }
        val vanligArbeidstid: Timer =
            arbeidsdager.map { it.vanligarbeidstid }.summer()

        val terskel = tellendedager.map { it.terskel() }.summer() / tellendedager.size.toDouble()
        val minsteTapteArbeidstid: Timer = terskel av vanligArbeidstid

        return arbeidstimer <= vanligArbeidstid - minsteTapteArbeidstid
    }
}

internal data class GjeldendeFastsettelser(
    val dag: Dag,
    val sats: BigDecimal,
    val rettighet: Dagpengerettighet,
    val vanligarbeidstid: Timer,
) {
    //
    fun terskel() = terskler[rettighet]?.get(dag.dato)
        ?: throw IllegalArgumentException("Fant ikke terskel for dag $dag og rettighet $rettighet")

    companion object {
        private val terskler: MutableMap<Dagpengerettighet, TemporalCollection<Prosent>> = mutableMapOf(
            Ordinær to TemporalCollection<Prosent>().also { it.put(LocalDate.of(2022, 4, 1), Prosent(50)) },
            ForskutterteLønnsgarantimidler to TemporalCollection<Prosent>().also {
                it.put(
                    LocalDate.of(2022, 4, 1),
                    Prosent(50),
                )
            },
            Permittering to TemporalCollection<Prosent>().also { it.put(LocalDate.of(2022, 4, 1), Prosent(50)) },
            PermitteringFraFiskeindustrien to TemporalCollection<Prosent>().also {
                it.put(
                    LocalDate.of(2012, 7, 1),
                    Prosent(40),
                )
            },

        )
    }
}
