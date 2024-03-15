package no.nav.dagpenger.behandling.mediator.repository

import kotliquery.queryOf
import kotliquery.sessionOf
import no.nav.dagpenger.behandling.db.PostgresDataSourceBuilder.dataSource
import no.nav.dagpenger.behandling.mediator.BehandlingRepository
import no.nav.dagpenger.behandling.mediator.PersonRepository
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

    override fun lagre(person: Person) =
        sessionOf(dataSource).use { session ->
            session.transaction { tx ->
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
                    // TODO: Denne må inn i samme transaksjon
                    behandlingRepository.lagre(behandling)

                    session.run(
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
}
