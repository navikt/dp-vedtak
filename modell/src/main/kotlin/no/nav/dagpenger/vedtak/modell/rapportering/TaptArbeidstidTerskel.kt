package no.nav.dagpenger.vedtak.modell.rapportering

import no.nav.dagpenger.vedtak.modell.TemporalCollection
import no.nav.dagpenger.vedtak.modell.entitet.Prosent
import no.nav.dagpenger.vedtak.modell.vedtak.rettighet.Rettighet
import java.time.LocalDate

internal object TaptArbeidstidTerskel {
    private val terskler: Map<Rettighet.RettighetType, TemporalCollection<Prosent>> =
        mapOf(
            Rettighet.RettighetType.Ordinær to
                TemporalCollection<Prosent>().apply {
                    put(
                        LocalDate.of(2022, 4, 1),
                        Prosent(50),
                    )
                },
            Rettighet.RettighetType.Permittering to
                TemporalCollection<Prosent>().apply {
                    put(
                        LocalDate.of(2022, 4, 1),
                        Prosent(50),
                    )
                },
            Rettighet.RettighetType.PermitteringFraFiskeindustrien to
                TemporalCollection<Prosent>().apply {
                    put(
                        LocalDate.of(2012, 7, 1),
                        Prosent(40),
                    )
                },
        )

    fun terskelFor(
        dagpengerettighet: Rettighet.RettighetType,
        dato: LocalDate,
    ): Prosent =
        terskler[dagpengerettighet]?.get(dato)
            ?: throw IllegalArgumentException("Fant ikke terskel for dato $dato og rettighet $dagpengerettighet")
}
