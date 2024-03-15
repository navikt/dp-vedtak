package no.nav.dagpenger.behandling.mediator.repository

import kotliquery.queryOf
import kotliquery.sessionOf
import no.nav.dagpenger.behandling.db.PostgresDataSourceBuilder
import no.nav.dagpenger.behandling.mediator.BehandlingRepository
import no.nav.dagpenger.behandling.modell.Behandling
import no.nav.dagpenger.behandling.modell.hendelser.SøknadInnsendtHendelse
import no.nav.dagpenger.opplysning.Opplysninger
import java.util.UUID

class BehandlingRepositoryPostgres() : BehandlingRepository {
    override fun hent(behandlingId: UUID): Behandling? {
        return sessionOf(PostgresDataSourceBuilder.dataSource).use {
            it.run(
                queryOf(
                    // language=PostgreSQL
                    """
                    SELECT *  
                    FROM behandling 
                    INNER JOIN behandler_hendelse_behandling ON behandling.behandling_id = behandler_hendelse_behandling.behandling_id
                    INNER JOIN behandler_hendelse ON behandler_hendelse_behandling.behandling_id = behandling.behandling_id                    
                    INNER JOIN behandling_opplysninger ON behandling_opplysninger.behandling_id = behandling.behandling_id                    
                    WHERE behandling.behandling_id = :id
                    """.trimIndent(),
                    mapOf(
                        "id" to behandlingId,
                    ),
                ).map { row ->
                    Behandling.rehydrer(
                        behandlingId = row.uuid("behandling_id"),
                        behandler =
                            when (row.string("hendelse_type")) {
                                SøknadInnsendtHendelse::class.simpleName ->
                                    SøknadInnsendtHendelse(
                                        ident = row.string("ident"),
                                        meldingsreferanseId = row.uuid("melding_id"),
                                        søknadId = UUID.fromString(row.string("ekstern_id")),
                                        gjelderDato = row.localDate("skjedde"),
                                    )
                                else -> throw IllegalArgumentException("Ukjent hendelse type")
                            },
                        aktiveOpplysninger = OpplysningerRepositoryPostgres().hentOpplysninger(row.uuid("opplysninger_id")).finnAlle(),
                    )
                }.asSingle,
            )
        }
    }

    override fun lagre(behandling: Behandling) {
        sessionOf(PostgresDataSourceBuilder.dataSource).use { session ->

            session.transaction { transactionalSession ->
                transactionalSession.run(
                    queryOf(
                        // language=PostgreSQL
                        """
                        INSERT INTO behandler_hendelse (ident, melding_id, ekstern_id, hendelse_type, skjedde) 
                        VALUES (:ident, :melding_id, :ekstern_id, :hendelse_type, :skjedde) ON CONFLICT DO NOTHING 
                        """.trimMargin(),
                        mapOf(
                            "ident" to behandling.behandler.ident,
                            "melding_id" to behandling.behandler.meldingsreferanseId,
                            "ekstern_id" to behandling.behandler.eksternId.id,
                            "hendelse_type" to behandling.behandler.type,
                            "skjedde" to behandling.behandler.skjedde,
                        ),
                    ).asUpdate,
                )
                transactionalSession.run(
                    queryOf(
                        // language=PostgreSQL
                        "INSERT INTO behandling (behandling_id) VALUES (:id)",
                        mapOf(
                            "id" to behandling.behandlingId,
                        ),
                    ).asUpdate,
                )
                transactionalSession.run(
                    queryOf(
                        // language=PostgreSQL
                        """
                        INSERT INTO behandler_hendelse_behandling (behandling_id, melding_id) 
                        VALUES (:behandling_id, :melding_id) ON CONFLICT DO NOTHING
                        """.trimIndent(),
                        mapOf(
                            "behandling_id" to behandling.behandlingId,
                            "melding_id" to behandling.behandler.meldingsreferanseId,
                        ),
                    ).asUpdate,
                )

                OpplysningerRepositoryPostgres().lagreOpplysninger(behandling.opplysninger() as Opplysninger)

                transactionalSession.run(
                    queryOf(
                        // language=PostgreSQL
                        """
                        INSERT INTO behandling_opplysninger (opplysninger_id, behandling_id) 
                        VALUES (:opplysninger_id, :behandling_id) ON CONFLICT DO NOTHING
                        """.trimIndent(),
                        mapOf(
                            "opplysninger_id" to behandling.opplysninger().id,
                            "behandling_id" to behandling.behandlingId,
                        ),
                    ).asUpdate,
                )

                behandling.basertPå.forEach { basertPåBehandling ->
                    transactionalSession.run(
                        queryOf(
                            // language=PostgreSQL
                            """
                            INSERT INTO behandling_basertpå (behandling_id, basert_på_behandling_id) 
                            VALUES (:behandling_id, :basert_paa_behandling_id) ON CONFLICT DO NOTHING
                            """.trimIndent(),
                            mapOf(
                                "behandling_id" to behandling.behandlingId,
                                "basert_på_behandling_id" to basertPåBehandling.behandlingId,
                            ),
                        ).asUpdate,
                    )
                }
            }
        }
    }
}
