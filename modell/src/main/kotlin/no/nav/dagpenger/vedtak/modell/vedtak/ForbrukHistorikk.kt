package no.nav.dagpenger.vedtak.modell.vedtak

import no.nav.dagpenger.vedtak.modell.TemporalCollection
import no.nav.dagpenger.vedtak.modell.mengde.Enhet.Companion.arbeidsdager
import no.nav.dagpenger.vedtak.modell.mengde.Stønadsperiode
import java.time.LocalDate

internal class ForbrukHistorikk : TemporalCollection<Stønadsperiode>() {
    fun summer(til: LocalDate): Stønadsperiode {
        val verdier = historiskeVerdier(til)
        return verdier.fold(0.arbeidsdager) { acc, ratioMengde -> acc + ratioMengde }
    }
}
