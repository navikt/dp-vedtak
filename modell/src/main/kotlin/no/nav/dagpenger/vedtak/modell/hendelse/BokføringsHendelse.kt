package no.nav.dagpenger.vedtak.modell.hendelse

import no.nav.dagpenger.vedtak.modell.Mengde
import no.nav.dagpenger.vedtak.modell.Person
import no.nav.dagpenger.vedtak.modell.konto.Postering
import java.time.LocalDate

abstract class BokføringsHendelse(val type: BokføringsHendelseType, val datoSett: LocalDate, val datoSkjedd: LocalDate, val person: Person) {

    private val bokføringer = mutableListOf<Postering>()

    fun leggTilPostering(postering: Postering) {
        bokføringer.add(postering)
    }

    internal fun finnBeregningsregel() = person.gjeldendeAvtale().finnBeregningsregel()

    fun håndter() {
        finnBeregningsregel().håndter(this)
    }
}

enum class BokføringsHendelseType {
    Meldekort,
    Kvotebruk
}

class Kvotebruk(internal val mengde: Mengde, datoSett: LocalDate, datoSkjedd: LocalDate, person: Person) : BokføringsHendelse(
    BokføringsHendelseType.Kvotebruk,
    datoSett,
    datoSkjedd,
    person
)
