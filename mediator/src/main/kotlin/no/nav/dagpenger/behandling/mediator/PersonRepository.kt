package no.nav.dagpenger.behandling.mediator

import no.nav.dagpenger.behandling.modell.Behandling
import no.nav.dagpenger.behandling.modell.Ident
import no.nav.dagpenger.behandling.modell.Person
import java.util.UUID

interface BehandlingRepository {
    fun hent(behandlingId: UUID): Behandling?

    fun lagre(behandling: Behandling)
}

interface PersonRepository : BehandlingRepository {
    fun hent(ident: Ident): Person?

    fun lagre(person: Person)
}
