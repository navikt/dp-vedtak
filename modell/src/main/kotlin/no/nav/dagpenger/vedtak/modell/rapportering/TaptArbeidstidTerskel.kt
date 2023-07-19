package no.nav.dagpenger.vedtak.modell.rapportering

import no.nav.dagpenger.vedtak.modell.Dagpengerettighet
import no.nav.dagpenger.vedtak.modell.TemporalCollection
import no.nav.dagpenger.vedtak.modell.entitet.Prosent
import java.time.LocalDate

internal object TaptArbeidstidTerskel {

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
