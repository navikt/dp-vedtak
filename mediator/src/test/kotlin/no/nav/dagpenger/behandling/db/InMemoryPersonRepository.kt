package no.nav.dagpenger.behandling.db

import no.nav.dagpenger.behandling.mediator.PersonRepository
import no.nav.dagpenger.behandling.modell.Person
import no.nav.dagpenger.behandling.modell.PersonIdentifikator

class InMemoryPersonRepository : PersonRepository {
    private val persondb = mutableMapOf<PersonIdentifikator, Person>()

    override fun hent(ident: PersonIdentifikator): Person? = persondb[ident]

    override fun lagre(person: Person) {
        persondb[person.ident()] = person
    }

    fun reset() {
        persondb.clear()
    }
}
