package no.nav.dagpenger.vedtak.mediator.persistens

import no.nav.dagpenger.vedtak.modell.Person
import no.nav.dagpenger.vedtak.modell.PersonIdentifikator

interface PersonRepository {
    fun hent(ident: PersonIdentifikator): Person?

    fun lagre(person: Person)
}
