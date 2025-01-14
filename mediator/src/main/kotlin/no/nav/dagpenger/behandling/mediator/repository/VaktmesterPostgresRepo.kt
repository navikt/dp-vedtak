package no.nav.dagpenger.behandling.mediator.repository

import kotliquery.Session
import kotliquery.TransactionalSession
import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import mu.KotlinLogging
import no.nav.dagpenger.behandling.db.PostgresDataSourceBuilder.dataSource
import java.util.UUID

private val logger = KotlinLogging.logger {}

internal class VaktmesterPostgresRepo {
    companion object {
        val låsenøkkel = 121212
        private val logger = KotlinLogging.logger {}
    }

    fun slettOpplysninger(antall: Int = 10): List<UUID> {
        val slettet = mutableListOf<UUID>()
        using(sessionOf(dataSource)) { session ->
            session.transaction { tx ->
                tx.medLås(låsenøkkel) {
                    hentAlleOpplysningerSomErFjernet(tx, antall).forEach { opplysninger ->
                        opplysninger.forEach { opplysningId ->
                            val liste = mutableListOf<BatchStatement>()
                            liste.add(slettOpplysningVerdi(opplysningId))
                            liste.add(slettOpplysningUtledet(opplysningId))
                            liste.add(slettOpplysningLink(opplysningId))
                            liste.add(slettOpplysningUtledning(opplysningId))
                            liste.add(slettErstatteAv(opplysningId))
                            liste.add(slettOpplysning(opplysningId))
                            liste.forEach { batch ->
                                batch.run(tx)
                            }
                            slettet.add(opplysningId)
                        }
                    }
                }
            }
        }
        return slettet
    }

    private fun hentAlleOpplysningerSomErFjernet(
        tx: TransactionalSession,
        antall: Int,
    ): List<List<UUID>> {
        val opplysningerIder = hentOpplysningerIder(tx, antall)

        //language=PostgreSQL
        val query =
            """
            SELECT id
            FROM opplysning
            INNER JOIN opplysninger_opplysning op ON opplysning.id = op.opplysning_id
            WHERE fjernet = true AND op.opplysninger_id = :opplysninger_id
            ORDER BY op.opplysninger_id, opprettet DESC;
            """.trimIndent()

        val opplysninger =
            opplysningerIder.map { id ->
                tx.run(
                    queryOf(
                        query,
                        mapOf("opplysninger_id" to id),
                    ).map { row ->
                        row.uuid("id")
                    }.asList,
                )
            }

        logger.info { "Fant ${opplysninger.size} opplysninger som er fjernet og som skal slettes" }
        return opplysninger
    }

    private fun hentOpplysningerIder(
        tx: TransactionalSession,
        antall: Int,
    ): List<UUID> {
        //language=PostgreSQL
        val test =
            """
            SELECT DISTINCT (op.opplysninger_id) AS opplysinger_id
            FROM opplysning
            INNER JOIN opplysninger_opplysning op ON opplysning.id = op.opplysning_id
            WHERE fjernet = true
            LIMIT :antall;
            """.trimIndent()

        val opplysningerIder =
            tx.run(
                queryOf(
                    test,
                    mapOf("antall" to antall),
                ).map { row ->
                    row.uuid("opplysinger_id")
                }.asList,
            )

        logger.info { "Skal slette opplysninger tilhørende opplysinger_id: ${opplysningerIder.joinToString("\n") { it.toString() }}" }
        return opplysningerIder
    }

    private fun slettErstatteAv(opplysningId: UUID) =
        BatchStatement(
            //language=PostgreSQL
            """
            DELETE FROM opplysning_erstattet_av WHERE erstattet_av = :id
            """.trimIndent(),
            listOf(mapOf("id" to opplysningId)),
        )

    private fun slettOpplysningVerdi(id: UUID) =
        BatchStatement(
            //language=PostgreSQL
            """
            DELETE FROM opplysning_verdi WHERE opplysning_id = :id
            """.trimIndent(),
            listOf(mapOf("id" to id)),
        )

    private fun slettOpplysningLink(id: UUID) =
        BatchStatement(
            //language=PostgreSQL
            """
            DELETE FROM opplysninger_opplysning WHERE opplysning_id = :id
            """.trimIndent(),
            listOf(mapOf("id" to id)),
        )

    private fun slettOpplysningUtledet(id: UUID) =
        BatchStatement(
            //language=PostgreSQL
            """
            DELETE FROM opplysning_utledet_av WHERE opplysning_id = :id
            """.trimIndent(),
            listOf(mapOf("id" to id)),
        )

    private fun slettOpplysningUtledning(id: UUID) =
        BatchStatement(
            //language=PostgreSQL
            """
            DELETE FROM opplysning_utledning WHERE opplysning_id = :id
            """.trimIndent(),
            listOf(mapOf("id" to id)),
        )

    private fun slettOpplysning(id: UUID) =
        BatchStatement(
            //language=PostgreSQL
            """
            DELETE FROM opplysning WHERE id = :id
            """.trimIndent(),
            listOf(mapOf("id" to id)),
        )
}

fun Session.lås(nøkkel: Int) =
    run(
        queryOf(
            //language=PostgreSQL
            """
            SELECT PG_TRY_ADVISORY_LOCK(:key)
            """.trimIndent(),
            mapOf("key" to nøkkel),
        ).map { res ->
            res.boolean("pg_try_advisory_lock")
        }.asSingle,
    ) ?: false

fun Session.låsOpp(nøkkel: Int) =
    run(
        queryOf(
            //language=PostgreSQL
            """
            SELECT PG_ADVISORY_UNLOCK(:key)
            """.trimIndent(),
            mapOf("key" to nøkkel),
        ).map { res ->
            res.boolean("pg_advisory_unlock")
        }.asSingle,
    ) ?: false

fun <T> Session.medLås(
    nøkkel: Int,
    block: () -> T,
): T? {
    if (!lås(nøkkel)) {
        logger.warn { "Fikk ikke lås for $nøkkel" }
        return null
    }
    return try {
        logger.info { "Fikk lås for $nøkkel" }
        block()
    } finally {
        logger.info { "Låser opp $nøkkel" }
        låsOpp(nøkkel)
    }
}
