package no.nav.dagpenger.vedtak.modell.hendelse

import no.nav.dagpenger.vedtak.modell.Mengde
import no.nav.dagpenger.vedtak.modell.Person
import java.time.LocalDate

class Kvotebruk(internal val mengde: Mengde, datoSett: LocalDate, datoSkjedd: LocalDate, person: Person) :
    BokføringsHendelse(
        BokføringsHendelseType.Kvotebruk,
        datoSett,
        datoSkjedd,
        person
    )
