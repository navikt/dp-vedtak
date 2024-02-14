package no.nav.dagpenger.behandling.mediator

import no.nav.dagpenger.behandling.modell.Person
import no.nav.dagpenger.behandling.modell.PersonIdentifikator

interface PersonRepository {
    fun hent(ident: PersonIdentifikator): Person?

    fun lagre(person: Person)
}
