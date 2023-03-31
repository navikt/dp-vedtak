package no.nav.dagpenger.vedtak.modell.rapportering

import no.nav.dagpenger.vedtak.modell.Dagpengerettighet
import no.nav.dagpenger.vedtak.modell.TemporalCollection
import no.nav.dagpenger.vedtak.modell.entitet.Prosent
import no.nav.dagpenger.vedtak.modell.entitet.Prosent.Companion.summer
import no.nav.dagpenger.vedtak.modell.entitet.Timer
import no.nav.dagpenger.vedtak.modell.entitet.Timer.Companion.summer
import no.nav.dagpenger.vedtak.modell.rapportering.Dag.Companion.summerArbeidstimer
import java.time.LocalDate

internal class TaptArbeidstid : Regel {

    fun håndter(periode: Beregningsgrunnlag): Boolean {
        val rettighetsdager = periode.rettighetsdager()
        val arbeidstimer: Timer = rettighetsdager.map { it.dag }.summerArbeidstimer()
        val arbeidsdagerMedRettighet = periode.arbeidsdagerMedRettighet()
        val vanligArbeidstid: Timer = arbeidsdagerMedRettighet.map { it.vanligArbeidstid() }.summer()

        val terskel: Prosent = arbeidsdagerMedRettighet.map { it.terskel() }.summer() / arbeidsdagerMedRettighet.size.toDouble()
        val minsteTapteArbeidstid: Timer = terskel av vanligArbeidstid

        return arbeidstimer <= vanligArbeidstid - minsteTapteArbeidstid
    }

    object Terskel {

        private val terskler: Map<Dagpengerettighet, TemporalCollection<Prosent>> = mapOf(
            Dagpengerettighet.Ordinær to TemporalCollection<Prosent>().also {
                it.put(
                    LocalDate.of(2022, 4, 1),
                    Prosent(50),
                )
            },
            Dagpengerettighet.ForskutterteLønnsgarantimidler to TemporalCollection<Prosent>().also {
                it.put(
                    LocalDate.of(2022, 4, 1),
                    Prosent(50),
                )
            },
            Dagpengerettighet.Permittering to TemporalCollection<Prosent>().also {
                it.put(
                    LocalDate.of(2022, 4, 1),
                    Prosent(50),
                )
            },
            Dagpengerettighet.PermitteringFraFiskeindustrien to TemporalCollection<Prosent>().also {
                it.put(
                    LocalDate.of(2012, 7, 1),
                    Prosent(40),
                )
            },

        )

        fun terskelFor(dagpengerettighet: Dagpengerettighet, dato: LocalDate): Prosent =
            terskler[dagpengerettighet]?.get(dato)
                ?: throw IllegalArgumentException("Fant ikke terskel for dato $dato og rettighet $dagpengerettighet")
    }
}
