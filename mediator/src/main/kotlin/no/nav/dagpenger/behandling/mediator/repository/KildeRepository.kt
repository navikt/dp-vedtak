package no.nav.dagpenger.behandling.mediator.repository

import kotliquery.Session
import kotliquery.queryOf
import kotliquery.sessionOf
import no.nav.dagpenger.behandling.db.PostgresDataSourceBuilder.dataSource
import no.nav.dagpenger.opplysning.Kilde
import no.nav.dagpenger.opplysning.Saksbehandlerkilde
import no.nav.dagpenger.opplysning.Systemkilde
import java.util.UUID

internal class KildeRepository {
    fun hentKilde(uuid: UUID): Kilde? = hentKilder(listOf(uuid))[uuid]

    fun hentKilder(uuid: List<UUID>): Map<UUID, Kilde> =
        sessionOf(dataSource)
            .use { session ->
                session.run(
                    queryOf(
                        //language=PostgreSQL
                        """
                        SELECT 
                            kilde.id, 
                            kilde.type, 
                            kilde.opprettet, 
                            kilde.registrert, 
                            kilde_system.melding_id AS system_melding_id, 
                            kilde_saksbehandler.ident AS saksbehandler_ident
                        FROM 
                            kilde 
                        LEFT JOIN 
                            kilde_system ON kilde.id = kilde_system.kilde_id
                        LEFT JOIN 
                            kilde_saksbehandler ON kilde.id = kilde_saksbehandler.kilde_id
                        WHERE kilde.id = ANY(?)
                        """.trimIndent(),
                        uuid.toTypedArray(),
                    ).map { row ->
                        val kildeId = row.uuid("id")
                        val kildeType = row.string("type")
                        val opprettet = row.localDateTime("opprettet")
                        val registrert = row.localDateTime("registrert")
                        when (kildeType) {
                            Systemkilde::class.java.simpleName ->
                                Systemkilde(
                                    row.uuid("system_melding_id"),
                                    opprettet,
                                    kildeId,
                                    registrert,
                                )

                            Saksbehandlerkilde::class.java.simpleName ->
                                Saksbehandlerkilde(
                                    row.string("saksbehandler_ident"),
                                    opprettet,
                                    kildeId,
                                    registrert,
                                )

                            else -> throw IllegalStateException("Ukjent kilde")
                        }
                    }.asList,
                )
            }.associateBy { it.id }

    fun lagreKilde(
        kilde: Kilde,
        tx: Session,
    ) = lagreKilde(listOf(kilde), tx)

    fun lagreKilde(
        kilder: List<Kilde>,
        tx: Session,
    ) {
        batchKilde(kilder).run(tx)
        require(kilder.all { it is Systemkilde || it is Saksbehandlerkilde }) { "Mangler lagring av kildetypen" }
        batchKildeSystem(kilder.filterIsInstance<Systemkilde>()).run(tx)
        batchKildeSaksbehandler(kilder.filterIsInstance<Saksbehandlerkilde>()).run(tx)
    }

    private fun batchKilde(kilder: List<Kilde>): BatchStatement =
        BatchStatement(
            // language=PostgreSQL
            """
            INSERT INTO kilde (id, type, opprettet, registrert) 
            VALUES (:id, :type, :opprettet, :registrert)
            ON CONFLICT DO NOTHING
            """.trimIndent(),
            kilder.map { kilde ->
                mapOf(
                    "id" to kilde.id,
                    "type" to kilde.javaClass.simpleName,
                    "opprettet" to kilde.opprettet,
                    "registrert" to kilde.registrert,
                )
            },
        )

    private fun batchKildeSystem(kilder: List<Systemkilde>) =
        BatchStatement(
            // language=PostgreSQL
            """
            INSERT INTO kilde_system (kilde_id, melding_id) 
            VALUES (:kildeId, :meldingId)
            ON CONFLICT DO NOTHING
            """.trimIndent(),
            kilder.map { kilde ->
                mapOf(
                    "kildeId" to kilde.id,
                    "meldingId" to kilde.meldingsreferanseId,
                )
            },
        )

    private fun batchKildeSaksbehandler(kilder: List<Saksbehandlerkilde>) =
        BatchStatement(
            // language=PostgreSQL
            """
            INSERT INTO kilde_saksbehandler (kilde_id, ident) 
            VALUES (:kildeId, :ident)
            ON CONFLICT DO NOTHING
            """.trimIndent(),
            kilder.map { kilde ->
                mapOf(
                    "kildeId" to kilde.id,
                    "ident" to kilde.ident,
                )
            },
        )
}
