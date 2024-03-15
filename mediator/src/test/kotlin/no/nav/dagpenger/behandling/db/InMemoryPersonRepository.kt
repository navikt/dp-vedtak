package no.nav.dagpenger.behandling.db

import no.nav.dagpenger.behandling.mediator.BehandlingRepository
import no.nav.dagpenger.behandling.mediator.PersonRepository
import no.nav.dagpenger.behandling.mediator.UnitOfWork
import no.nav.dagpenger.behandling.modell.Behandling
import no.nav.dagpenger.behandling.modell.Ident
import no.nav.dagpenger.behandling.modell.Person
import java.util.UUID

class InMemoryPersonRepository : PersonRepository, BehandlingRepository {
    private val persondb = mutableMapOf<Ident, Person>()

    override fun hent(ident: Ident): Person? = persondb[ident]

    override fun hent(behandlingId: UUID): Behandling? {
        return persondb.values.flatMap { it.behandlinger() }.find { it.behandlingId == behandlingId }
    }

    override fun lagre(behandling: Behandling) {
        TODO("Not yet implemented")
    }

    override fun lagre(
        behandling: Behandling,
        unitOfWork: UnitOfWork<*>,
    ) {
        TODO("Not yet implemented")
    }

    override fun lagre(person: Person) {
        persondb[person.ident] = person
    }

    override fun lagre(
        person: Person,
        unitOfWork: UnitOfWork<*>,
    ) {
        TODO("Not yet implemented")
    }

    fun reset() {
        persondb.clear()
    }
}
