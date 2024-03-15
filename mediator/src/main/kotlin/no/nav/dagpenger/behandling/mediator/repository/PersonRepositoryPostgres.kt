package no.nav.dagpenger.behandling.mediator.repository

import kotliquery.queryOf
import kotliquery.sessionOf
import no.nav.dagpenger.behandling.db.PostgresDataSourceBuilder.dataSource
import no.nav.dagpenger.behandling.mediator.BehandlingRepository
import no.nav.dagpenger.behandling.mediator.PersonRepository
import no.nav.dagpenger.behandling.modell.Person
import no.nav.dagpenger.behandling.modell.PersonIdentifikator

class PersonRepositoryPostgres(
    private val behandlingRepository: BehandlingRepository,
) : PersonRepository, BehandlingRepository by behandlingRepository {
    override fun hent(ident: PersonIdentifikator): Person? {
        return sessionOf(dataSource).use { session ->
            session.run(
                queryOf(
                    //language=PostgreSQL
                    """
                    SELECT * FROM person_view WHERE identer IN (:ident)
                    """.trimIndent(),
                    mapOf("ident" to ident.identifikator()),
                ).map { row ->
                    Person(ident = PersonIdentifikator(row.string("ident")))
                }.asSingle,
            )
        }
    }

    override fun lagre(person: Person) {
        TODO("Not yet implemented")
    }
}
