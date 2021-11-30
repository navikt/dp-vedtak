package no.nav.dagpenger.vedtak.modell.hendelse

import no.nav.dagpenger.vedtak.modell.Person
import no.nav.dagpenger.vedtak.modell.konto.Postering
import java.time.LocalDate

abstract class BokføringsHendelse(
    private val type: BokføringsHendelseType,
    val datoSett: LocalDate,
    private val datoSkjedd: LocalDate,
    private val person: Person
) {
    private val bokføringer = mutableListOf<Postering>()

    fun leggTilPostering(postering: Postering) {
        bokføringer.add(postering)
    }

    private fun finnBeregningsregel() = person.gjeldendeAvtale().finnBeregningsregel(type, datoSkjedd)

    fun håndter() {
        finnBeregningsregel()?.håndter(this)
    }
}

enum class BokføringsHendelseType {
    Meldekort,
    Kvotebruk
}
