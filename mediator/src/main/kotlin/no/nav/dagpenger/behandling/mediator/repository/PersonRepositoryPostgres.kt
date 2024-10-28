package no.nav.dagpenger.behandling.mediator.repository

import kotliquery.queryOf
import kotliquery.sessionOf
import mu.KotlinLogging
import no.nav.dagpenger.behandling.db.PostgresDataSourceBuilder.dataSource
import no.nav.dagpenger.behandling.mediator.Metrikk
import no.nav.dagpenger.behandling.modell.Ident
import no.nav.dagpenger.behandling.modell.Person

class PersonRepositoryPostgres(
    private val behandlingRepository: BehandlingRepository,
) : PersonRepository,
    BehandlingRepository by behandlingRepository {
    private companion object {
        val logger = KotlinLogging.logger { }
    }

    override fun hent(ident: Ident) =
        sessionOf(dataSource).use { session ->
            session.run(
                queryOf(
                    //language=PostgreSQL
                    """
                    SELECT * FROM person WHERE ident = :ident FOR UPDATE
                    """.trimIndent(),
                    mapOf("ident" to ident.identifikator()),
                ).map { row ->
                    val dbIdent = Ident(row.string("ident"))
                    val behandlinger = behandlingerFor(dbIdent)
                    logger.info { "Hentet person med ${behandlinger.size} behandlinger" }
                    Metrikk.registrerAntallBehandlinger(behandlinger.size)
                    Person(dbIdent, behandlinger)
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
                    behandlingRepository.hentBehandling(row.uuid("behandling_id"))
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
