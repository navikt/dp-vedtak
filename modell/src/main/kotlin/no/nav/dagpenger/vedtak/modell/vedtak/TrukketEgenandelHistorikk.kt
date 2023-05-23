package no.nav.dagpenger.vedtak.modell.vedtak

import no.nav.dagpenger.vedtak.modell.TemporalCollection
import no.nav.dagpenger.vedtak.modell.entitet.Beløp
import no.nav.dagpenger.vedtak.modell.entitet.Beløp.Companion.summerBeløp
import java.time.LocalDate

internal class TrukketEgenandelHistorikk : TemporalCollection<Beløp>() {
    fun summer(til: LocalDate): Beløp {
        val verdier = historiskeVerdier(til)
        return verdier.summerBeløp()
    }
}
