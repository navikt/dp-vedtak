package no.nav.dagpenger.vedtak.modell.vedtak

import no.nav.dagpenger.vedtak.modell.TemporalCollection
import no.nav.dagpenger.vedtak.modell.entitet.Stønadsdager
import java.time.LocalDate

internal class ForbrukHistorikk : TemporalCollection<Stønadsdager>() {
    fun summer(til: LocalDate): Stønadsdager {
        val verdier = historiskeVerdier(til)
        return verdier.fold(Stønadsdager(0)) { acc, ratioMengde -> acc + ratioMengde }
    }
}
