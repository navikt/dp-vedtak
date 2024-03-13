package no.nav.dagpenger.behandling.mediator.repository

import no.nav.dagpenger.behandling.mediator.BehandlingRepository
import no.nav.dagpenger.behandling.mediator.PersonRepository
import no.nav.dagpenger.behandling.modell.Behandling
import no.nav.dagpenger.behandling.modell.Person
import no.nav.dagpenger.behandling.modell.PersonIdentifikator
import java.util.UUID

class InMemoryPersonRepository(private val opplysningRepository: OpplysningRepository = OpplysningRepositoryPostgres()) :
    PersonRepository,
    BehandlingRepository {
    private val persondb = mutableMapOf<PersonIdentifikator, Person>()

    override fun hent(ident: PersonIdentifikator): Person? = persondb[ident]

    override fun hent(behandlingId: UUID): Behandling? {
        return persondb.values.flatMap { it.behandlinger() }.find { it.behandlingId == behandlingId }
    }

    override fun lagre(person: Person) {
        persondb[person.ident()] = person
        opplysningRepository.lagreOpplysninger(person.behandlinger().flatMap { it.opplysninger().finnAlle() })
    }

    fun reset() {
        persondb.clear()
    }
}
