package no.nav.dagpenger.vedtak.modell.hendelse

import no.nav.dagpenger.vedtak.modell.Person
import no.nav.dagpenger.vedtak.modell.mengder.Tid
import java.time.LocalDate

class Kvotebruk(internal val mengde: Tid, datoSett: LocalDate, datoSkjedd: LocalDate, person: Person) :
    BokføringsHendelse(
        BokføringsHendelseType.Kvotebruk,
        datoSett,
        datoSkjedd,
        person
    )
