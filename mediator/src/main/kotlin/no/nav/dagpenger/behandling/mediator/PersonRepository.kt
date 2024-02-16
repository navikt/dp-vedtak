package no.nav.dagpenger.behandling.mediator

import no.nav.dagpenger.behandling.modell.Behandling
import no.nav.dagpenger.behandling.modell.Person
import no.nav.dagpenger.behandling.modell.PersonIdentifikator
import java.util.UUID

interface BehandlingRepository {
    fun hent(behandlingId: UUID): Behandling?
}

interface PersonRepository : BehandlingRepository {
    fun hent(ident: PersonIdentifikator): Person?

    fun lagre(person: Person)
}
