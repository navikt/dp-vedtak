package no.nav.dagpenger.behandling.mediator.repository

import no.nav.dagpenger.avklaring.Avklaring
import no.nav.dagpenger.behandling.modell.Behandling
import no.nav.dagpenger.behandling.modell.Ident
import no.nav.dagpenger.behandling.modell.Person
import java.util.UUID

interface AvklaringRepository {
    fun lagreAvklaringer(
        behandling: Behandling,
        unitOfWork: UnitOfWork<*>,
    )

    fun hentAvklaringer(behandlingId: UUID): List<Avklaring>
}

interface BehandlingRepository : AvklaringRepository {
    fun hentBehandling(behandlingId: UUID): Behandling?

    fun lagre(behandling: Behandling)

    fun lagre(
        behandling: Behandling,
        unitOfWork: UnitOfWork<*>,
    )
}

interface PersonRepository : BehandlingRepository {
    fun hent(ident: Ident): Person?

    fun lagre(person: Person)

    fun lagre(
        person: Person,
        unitOfWork: UnitOfWork<*>,
    )
}

interface UnitOfWork<S> {
    fun <T> inTransaction(block: (S) -> T): T

    fun rollback()

    fun commit()
}
