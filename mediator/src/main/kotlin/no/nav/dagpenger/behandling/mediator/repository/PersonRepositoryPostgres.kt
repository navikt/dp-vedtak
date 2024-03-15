package no.nav.dagpenger.behandling.mediator.repository

import kotliquery.queryOf
import kotliquery.sessionOf
import no.nav.dagpenger.behandling.db.PostgresDataSourceBuilder.dataSource
import no.nav.dagpenger.behandling.mediator.BehandlingRepository
import no.nav.dagpenger.behandling.mediator.PersonRepository
import no.nav.dagpenger.behandling.mediator.PostgresUnitOfWork
import no.nav.dagpenger.behandling.mediator.UnitOfWork
import no.nav.dagpenger.behandling.modell.Ident
import no.nav.dagpenger.behandling.modell.Person

class PersonRepositoryPostgres(
    private val behandlingRepository: BehandlingRepository,
) : PersonRepository, BehandlingRepository by behandlingRepository {
    override fun hent(ident: Ident) =
        sessionOf(dataSource).use { session ->
            session.run(
                queryOf(
                    //language=PostgreSQL
                    """
                    SELECT * FROM person WHERE ident = :ident
                    """.trimIndent(),
                    mapOf("ident" to ident.identifikator()),
                ).map { row ->
                    val ident = Ident(row.string("ident"))
                    val behandlinger = behandlingerFor(ident)
                    Person(ident, behandlinger)
                }.asSingle,
            )
        }

    private fun behandlingerFor(ident: Ident) =
        sessionOf(dataSource).use { session ->
            session.run(
                queryOf(
                    //language=PostgreSQL
                    """
                    SELECT * FROM person_behandling WHERE ident IN (:ident)
                    """.trimIndent(),
                    mapOf("ident" to ident.alleIdentifikatorer().first()),
                ).map { row ->
                    behandlingRepository.hent(row.uuid("behandling_id"))
                }.asList,
            )
        }

    override fun lagre(person: Person) {
        val unitOfWork = PostgresUnitOfWork.transaction()
        lagre(person, unitOfWork)
        unitOfWork.commit()
    }

    override fun lagre(
        person: Person,
        unitOfWork: UnitOfWork<*>,
    ) = lagre(person, unitOfWork as PostgresUnitOfWork)

    private fun lagre(
        person: Person,
        unitOfWork: PostgresUnitOfWork,
    ) = unitOfWork.inTransaction { tx ->
        tx.run(
            queryOf(
                //language=PostgreSQL
                """
                INSERT INTO person (ident) VALUES (:ident) ON CONFLICT DO NOTHING
                """.trimIndent(),
                mapOf("ident" to person.ident.identifikator()),
            ).asUpdate,
        )
        person.behandlinger().forEach { behandling ->
            behandlingRepository.lagre(behandling, unitOfWork)

            tx.run(
                queryOf(
                    //language=PostgreSQL
                    """
                    INSERT INTO person_behandling (ident, behandling_id)
                    VALUES (:ident, :behandling_id)
                    ON CONFLICT DO NOTHING
                    """.trimIndent(),
                    mapOf(
                        "ident" to person.ident.identifikator(),
                        "behandling_id" to behandling.behandlingId,
                    ),
                ).asUpdate,
            )
        }
    }
}
