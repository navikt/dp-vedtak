package no.nav.dagpenger.behandling.mediator.repository

import kotliquery.Session
import kotliquery.queryOf
import kotliquery.sessionOf
import no.nav.dagpenger.behandling.db.PostgresDataSourceBuilder.dataSource
import no.nav.dagpenger.behandling.modell.Arbeidssteg
import no.nav.dagpenger.behandling.modell.Behandling
import no.nav.dagpenger.opplysning.Opplysninger
import no.nav.dagpenger.opplysning.Saksbehandler
import no.nav.dagpenger.regel.SøknadInnsendtHendelse
import java.util.UUID

class BehandlingRepositoryPostgres(
    private val opplysningRepository: OpplysningerRepository,
    private val avklaringRepository: AvklaringRepository,
) : BehandlingRepository,
    AvklaringRepository by avklaringRepository {
    override fun hentBehandling(behandlingId: UUID): Behandling? =
        sessionOf(dataSource).use { session ->
            session.run(
                queryOf(
                    // language=PostgreSQL
                    """
                    SELECT *  
                    FROM behandling 
                    LEFT JOIN behandler_hendelse_behandling ON behandling.behandling_id = behandler_hendelse_behandling.behandling_id
                    LEFT JOIN behandler_hendelse ON behandler_hendelse.melding_id = behandler_hendelse_behandling.melding_id
                    LEFT JOIN behandling_opplysninger ON behandling.behandling_id = behandling_opplysninger.behandling_id                    
                    WHERE behandling.behandling_id = :id 
                    """.trimIndent(),
                    mapOf(
                        "id" to behandlingId,
                    ),
                ).map { row ->
                    val basertPåBehandlingId = session.hentBasertPåFor(behandlingId)
                    val basertPåBehandling = basertPåBehandlingId.mapNotNull { id -> hentBehandling(id) }

                    Behandling.rehydrer(
                        behandlingId = row.uuid("behandling_id"),
                        behandler =
                            when (row.string("hendelse_type")) {
                                // TODO: Dette er ganske gnøkka. Modellen definerer bare StartHendelse, men vi må lage den konkrete fra Dagpenger
                                SøknadInnsendtHendelse::class.simpleName ->
                                    SøknadInnsendtHendelse(
                                        meldingsreferanseId = row.uuid("melding_id"),
                                        ident = row.string("ident"),
                                        søknadId = UUID.fromString(row.string("ekstern_id")),
                                        gjelderDato = row.localDate("skjedde"),
                                        fagsakId = row.int("fagsak_id"),
                                        opprettet = row.localDateTime("opprettet"),
                                    )

                                else -> throw IllegalArgumentException("Ukjent hendelse type ${row.string("hendelse_type")}")
                            },
                        gjeldendeOpplysninger = opplysningRepository.hentOpplysninger(row.uuid("opplysninger_id"))!!,
                        basertPå = basertPåBehandling,
                        tilstand = Behandling.TilstandType.valueOf(row.string("tilstand")),
                        sistEndretTilstand = row.localDateTime("sist_endret_tilstand"),
                        avklaringer = hentAvklaringer(behandlingId),
                        godkjent = session.hentArbeidssteg(behandlingId, "godkjent"),
                        besluttet = session.hentArbeidssteg(behandlingId, "besluttet"),
                    )
                }.asSingle,
            )
        }

    private fun Session.hentArbeidssteg(
        behandlingId: UUID,
        steg: String,
    ) = this.run(
        queryOf(
            """
            SELECT * 
            FROM behandling_arbeidssteg 
            WHERE behandling_id = :behandling_id
            AND oppgave = :steg
            """.trimIndent(),
            mapOf(
                "behandling_id" to behandlingId,
                "steg" to steg,
            ),
        ).map { row ->
            if (row.stringOrNull("utført_av") == null) {
                return@map null
            }
            Arbeidssteg(
                utførtAv = Saksbehandler(row.string("utført_av")),
                utført = row.localDateTime("utført"),
            )
        }.asSingle,
    )

    private fun Session.hentBasertPåFor(behandlingId: UUID) =
        this.run(
            queryOf(
                // language=PostgreSQL
                """
                SELECT *  
                FROM behandling_basertpå 
                WHERE behandling_id = :id 
                """.trimIndent(),
                mapOf(
                    "id" to behandlingId,
                ),
            ).map { row ->
                row.uuid("basert_på_behandling_id")
            }.asList,
        )

    override fun lagre(behandling: Behandling) {
        val unitOfWork = PostgresUnitOfWork.transaction()
        lagre(behandling, unitOfWork)
        unitOfWork.commit()
    }

    override fun lagre(
        behandling: Behandling,
        unitOfWork: UnitOfWork<*>,
    ) = lagre(behandling, unitOfWork as PostgresUnitOfWork)

    private fun lagre(
        behandling: Behandling,
        unitOfWork: PostgresUnitOfWork,
    ) {
        unitOfWork.inTransaction { tx ->
            tx.run(
                queryOf(
                    // language=PostgreSQL
                    """
                        INSERT INTO behandler_hendelse (ident, melding_id, ekstern_id, hendelse_type, skjedde, fagsak_id) 
                        VALUES (:ident, :melding_id, :ekstern_id, :hendelse_type, :skjedde, :fagsak_id) ON CONFLICT DO NOTHING 
                    """.trimMargin(),
                    mapOf(
                        "ident" to behandling.behandler.ident,
                        "melding_id" to behandling.behandler.meldingsreferanseId,
                        "ekstern_id" to behandling.behandler.eksternId.id,
                        "hendelse_type" to behandling.behandler.type,
                        "skjedde" to behandling.behandler.skjedde,
                        "fagsak_id" to behandling.behandler.fagsakId,
                    ),
                ).asUpdate,
            )
            tx.run(
                queryOf(
                    // language=PostgreSQL
                    """
                    INSERT INTO behandling (behandling_id, tilstand, sist_endret_tilstand)
                    VALUES (:id, :tilstand, :sisteEndretTilstand)
                    ON CONFLICT (behandling_id) DO UPDATE SET tilstand = :tilstand, sist_endret_tilstand = :sisteEndretTilstand
                    """.trimIndent(),
                    mapOf(
                        "id" to behandling.behandlingId,
                        "tilstand" to behandling.tilstand().first.name,
                        "sisteEndretTilstand" to behandling.tilstand().second,
                    ),
                ).asUpdate,
            )
            tx.run(
                queryOf(
                    //language=PostgreSQL
                    """
                    INSERT INTO behandling_tilstand (behandling_id, tilstand, endret)
                    VALUES (:behandling_id, :tilstand, :endret)
                    ON CONFLICT DO NOTHING
                    """.trimIndent(),
                    mapOf(
                        "behandling_id" to behandling.behandlingId,
                        "tilstand" to behandling.tilstand().first.name,
                        "endret" to behandling.tilstand().second,
                    ),
                ).asUpdate,
            )
            tx.run(
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
            tx.run(
                queryOf(
                    // language=PostgreSQL
                    """
                    INSERT INTO behandling_arbeidssteg(behandling_id, oppgave, utført_av, utført) 
                    VALUES (:behandling_id, :oppgave, :utfort_av, :utfort) ON CONFLICT (behandling_id, oppgave) DO UPDATE SET utført_av = :utfort_av, utført = :utfort 
                    """.trimIndent(),
                    mapOf(
                        "behandling_id" to behandling.behandlingId,
                        "oppgave" to "godkjent",
                        "utfort_av" to behandling.godkjent?.utførtAv?.ident,
                        "utfort" to behandling.godkjent?.utført,
                    ),
                ).asUpdate,
            )
            tx.run(
                queryOf(
                    // language=PostgreSQL
                    """
                    INSERT INTO behandling_arbeidssteg(behandling_id, oppgave, utført_av, utført) 
                    VALUES (:behandling_id, :oppgave, :utfort_av, :utfort) ON CONFLICT (behandling_id, oppgave) DO UPDATE SET utført_av = :utfort_av, utført = :utfort 
                    """.trimIndent(),
                    mapOf(
                        "behandling_id" to behandling.behandlingId,
                        "oppgave" to "besluttet",
                        "utfort_av" to behandling.besluttet?.utførtAv?.ident,
                        "utfort" to behandling.besluttet?.utført,
                    ),
                ).asUpdate,
            )

            opplysningRepository.lagreOpplysninger(behandling.opplysninger() as Opplysninger, unitOfWork)

            tx.run(
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
                tx.run(
                    queryOf(
                        // language=PostgreSQL
                        """
                        INSERT INTO behandling_basertpå (behandling_id, basert_på_behandling_id) 
                        VALUES (:behandling_id, :basert_paa_behandling_id) ON CONFLICT DO NOTHING
                        """.trimIndent(),
                        mapOf(
                            "behandling_id" to behandling.behandlingId,
                            "basert_paa_behandling_id" to basertPåBehandling.behandlingId,
                        ),
                    ).asUpdate,
                )
            }

            avklaringRepository.lagreAvklaringer(behandling, unitOfWork)
        }
    }
}
