package no.nav.dagpenger.behandling.mediator.repository

import kotliquery.Session
import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import mu.KotlinLogging
import mu.withLoggingContext
import no.nav.dagpenger.behandling.db.PostgresDataSourceBuilder.dataSource
import java.util.UUID

private val logger = KotlinLogging.logger {}

internal class VaktmesterPostgresRepo {
    companion object {
        val låsenøkkel = 121212
        private val logger = KotlinLogging.logger {}
    }

    fun slettOpplysninger(antall: Int = 1): List<UUID> {
        val slettet = mutableListOf<UUID>()
        using(sessionOf(dataSource)) { session ->
            val kandidater = hentAlleOpplysningerSomErFjernet(session, antall)
            kandidater.forEach { kandidat ->
                session.transaction { tx ->
                    tx.medLås(låsenøkkel) {
                        withLoggingContext(
                            "behandlingId" to kandidat.behandlingId.toString(),
                            "opplysningerId" to kandidat.opplysningerId.toString(),
                        ) {
                            logger.info { "Skal slette ${kandidat.opplysninger().size} opplysninger " }
                            kandidat.opplysninger().forEach { opplysningId ->
                                val statements = mutableListOf<BatchStatement>()
                                statements.add(slettOpplysningVerdi(opplysningId))
                                statements.add(slettOpplysningUtledet(opplysningId))
                                statements.add(slettOpplysningLink(opplysningId))
                                statements.add(slettOpplysningUtledning(opplysningId))
                                statements.add(slettErstatteAv(opplysningId))
                                statements.add(slettOpplysning(opplysningId))
                                statements.forEach { batch ->
                                    batch.run(tx)
                                }
                                slettet.add(opplysningId)
                            }
                            logger.info { "Slettet ${kandidat.opplysninger().size} opplysninger" }
                        }
                    }
                }
            }
        }
        return slettet
    }

    internal data class Kandidat(
        val behandlingId: UUID?,
        val opplysningerId: UUID,
        private val opplysninger: MutableList<UUID> = mutableListOf(),
    ) {
        fun leggTil(uuid: UUID) {
            opplysninger.add(uuid)
        }

        fun opplysninger() = opplysninger.toList()
    }

    private fun hentAlleOpplysningerSomErFjernet(
        session: Session,
        antall: Int,
    ): List<Kandidat> {
        val kandidater = hentOpplysningerIder(session, antall)

        //language=PostgreSQL
        val query =
            """
            SELECT id
            FROM opplysning
            INNER JOIN opplysninger_opplysning op ON opplysning.id = op.opplysning_id
            WHERE fjernet = TRUE AND op.opplysninger_id = :opplysninger_id
            ORDER BY op.opplysninger_id, opprettet DESC;
            """.trimIndent()

        val opplysninger =
            kandidater
                .onEach { kandidat ->
                    session.run(
                        queryOf(
                            query,
                            mapOf("opplysninger_id" to kandidat.opplysningerId),
                        ).map { row ->
                            kandidat.leggTil(
                                row.uuid("id"),
                            )
                        }.asList,
                    )
                }
        logger.info {
            val antallOpplysinger: Int =
                kandidater
                    .map {
                        it.opplysninger().size
                    }.reduce { acc, i -> acc + i }
            "Fant ${kandidater.size} opplysningsett for behandlinger ${kandidater.map {
                it.behandlingId
            }} som inneholder $antallOpplysinger opplysninger som er fjernet og som skal slettes"
        }
        return opplysninger
    }

    private fun hentOpplysningerIder(
        session: Session,
        antall: Int,
    ): List<Kandidat> {
        //language=PostgreSQL
        val test =
            """
            SELECT DISTINCT (op.opplysninger_id) AS opplysinger_id, b.behandling_id
            FROM opplysning
                INNER JOIN opplysninger_opplysning op ON opplysning.id = op.opplysning_id
                LEFT OUTER JOIN behandling_opplysninger b ON b.opplysninger_id = op.opplysninger_id
            WHERE fjernet = TRUE AND op.opplysninger_id != '01932f46-c4d3-755e-a4da-c572945a93b5'
            LIMIT :antall;
            """.trimIndent()

        val opplysningerIder =
            session.run(
                queryOf(
                    test,
                    mapOf("antall" to antall),
                ).map { row ->
                    Kandidat(
                        row.uuidOrNull("behandling_id"),
                        row.uuid("opplysinger_id"),
                    )
                }.asList,
            )
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
