package no.nav.dagpenger.vedtak.modell.rapportering

import no.nav.dagpenger.vedtak.modell.Dagpengerettighet
import no.nav.dagpenger.vedtak.modell.TemporalCollection
import no.nav.dagpenger.vedtak.modell.entitet.Prosent
import no.nav.dagpenger.vedtak.modell.entitet.Prosent.Companion.summer
import no.nav.dagpenger.vedtak.modell.entitet.Timer
import java.time.LocalDate

internal class TaptArbeidstid : Regel {

    fun håndter(beregningsgrunnlag: Beregningsgrunnlag): Boolean {
        val arbeidstimer = beregningsgrunnlag.arbeidedeTimer()
        val arbeidsdagerMedRettighet = beregningsgrunnlag.mandagTilFredagMedRettighet()
        val vanligArbeidstid: Timer = beregningsgrunnlag.vanligArbeidstid()

        val terskel: Prosent = arbeidsdagerMedRettighet.map { it.terskelTaptArbeidstid() }.summer() / arbeidsdagerMedRettighet.size.toDouble()
        val minsteTapteArbeidstid: Timer = terskel av vanligArbeidstid

        return arbeidstimer <= vanligArbeidstid - minsteTapteArbeidstid
    }

    internal object Terskel {

        private val terskler: Map<Dagpengerettighet, TemporalCollection<Prosent>> = mapOf(
            Dagpengerettighet.Ordinær to TemporalCollection<Prosent>().apply {
                put(
                    LocalDate.of(2022, 4, 1),
                    Prosent(50),
                )
            },
            Dagpengerettighet.ForskutterteLønnsgarantimidler to TemporalCollection<Prosent>().apply {
                put(
                    LocalDate.of(2022, 4, 1),
                    Prosent(50),
                )
            },
            Dagpengerettighet.Permittering to TemporalCollection<Prosent>().apply {
                put(
                    LocalDate.of(2022, 4, 1),
                    Prosent(50),
                )
            },
            Dagpengerettighet.PermitteringFraFiskeindustrien to TemporalCollection<Prosent>().apply {
                put(
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
