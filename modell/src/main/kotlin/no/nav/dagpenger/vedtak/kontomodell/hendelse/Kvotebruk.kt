package no.nav.dagpenger.vedtak.kontomodell.hendelse

import no.nav.dagpenger.vedtak.kontomodell.Person
import no.nav.dagpenger.vedtak.kontomodell.mengder.Tid
import java.time.LocalDate

class Kvotebruk(internal val mengde: Tid, datoSett: LocalDate, datoSkjedd: LocalDate, person: Person) :
    BokføringsHendelse(
        BokføringsHendelseType.Kvotebruk,
        datoSett,
        datoSkjedd,
        person,
    )
