package no.nav.dagpenger.behandling.mediator.repository

import kotliquery.Session
import kotliquery.queryOf
import kotliquery.sessionOf
import no.nav.dagpenger.avklaring.Avklaring
import no.nav.dagpenger.avklaring.Avklaringkode
import no.nav.dagpenger.behandling.db.PostgresDataSourceBuilder.dataSource
import no.nav.dagpenger.behandling.mediator.repository.AvklaringRepositoryObserver.NyAvklaringHendelse
import no.nav.dagpenger.behandling.modell.Behandling
import java.util.UUID

internal class AvklaringRepositoryPostgres private constructor(
    private val observatører: MutableList<AvklaringRepositoryObserver>,
) : AvklaringRepository {
    constructor(vararg observatører: AvklaringRepositoryObserver) : this(observatører.toMutableList())

    override fun lagreAvklaringer(
        behandling: Behandling,
        unitOfWork: UnitOfWork<*>,
    ) {
        lagre(behandling, unitOfWork as PostgresUnitOfWork)
    }

    override fun hentAvklaringer(behandlingId: UUID) =
        sessionOf(dataSource).use { session ->
            session.run(
                queryOf(
                    // language=PostgreSQL
                    """
                    SELECT id, avklaringkode.*
                    FROM avklaring
                             LEFT JOIN avklaringkode ON avklaring.avklaring_kode = avklaringkode.kode
                    WHERE behandling_id = :behandling_id
                    """.trimIndent(),
                    mapOf(
                        "behandling_id" to behandlingId,
                    ),
                ).map { row ->
                    Avklaring(
                        id = row.uuid("id"),
                        kode =
                            Avklaringkode(
                                kode = row.string("kode"),
                                tittel = row.string("tittel"),
                                beskrivelse = row.string("beskrivelse"),
                                kanKvitteres = row.boolean("kan_kvitteres"),
                            ),
                        historikk = session.hentEndringer(row.uuid("id")).toMutableList(),
                    )
                }.asList,
            )
        }

    private enum class EndringType {
        UnderBehandling,
        Avklart,
        Avbrutt,
    }

    private fun Session.hentEndringer(uuid: UUID): List<Avklaring.Endring> =
        run(
            queryOf(
                // language=PostgreSQL
                """
                SELECT * FROM avklaring_endring WHERE avklaring_id = :avklaring_id
                """.trimIndent(),
                mapOf("avklaring_id" to uuid),
            ).map {
                val id = it.uuid("endring_id")
                val endret = it.localDateTime("endret")
                when (EndringType.valueOf(it.string("type"))) {
                    EndringType.UnderBehandling -> Avklaring.Endring.UnderBehandling(id, endret)
                    EndringType.Avklart -> Avklaring.Endring.Avklart(id, it.string("saksbehandler"), endret)
                    EndringType.Avbrutt -> Avklaring.Endring.Avbrutt(id, endret)
                }
            }.asList,
        )

    private fun lagre(
        behandling: Behandling,
        unitOfWork: PostgresUnitOfWork,
    ) {
        val avklaringer = behandling.aktiveAvklaringer
        val nyeAvklaringer = mutableListOf<Avklaring>()

        unitOfWork.inTransaction { tx ->
            avklaringer.forEach { avklaring ->
                val avklaringskode = avklaring.kode
                tx.run(
                    queryOf(
                        // language=PostgreSQL
                        """
                        INSERT INTO avklaringkode (kode, tittel, beskrivelse, kan_kvitteres)
                        VALUES (:kode, :tittel, :beskrivelse, :kanKvitteres)
                        ON CONFLICT (kode) DO UPDATE SET tittel = :tittel, beskrivelse = :beskrivelse, kan_kvitteres = :kanKvitteres
                        """.trimIndent(),
                        mapOf(
                            "kode" to avklaringskode.kode,
                            "tittel" to avklaringskode.tittel,
                            "beskrivelse" to avklaringskode.beskrivelse,
                            "kanKvitteres" to avklaringskode.kanKvitteres,
                        ),
                    ).asUpdate,
                )
                val lagret =
                    tx.run(
                        queryOf(
                            // language=PostgreSQL
                            """
                            INSERT INTO avklaring (id, behandling_id, avklaring_kode)
                            VALUES (:avklaring_id, :behandling_id, :avklaring_kode)
                            ON CONFLICT (id) DO NOTHING
                            """.trimIndent(),
                            mapOf(
                                "avklaring_id" to avklaring.id,
                                "behandling_id" to behandling.behandlingId,
                                "avklaring_kode" to avklaring.kode.kode,
                            ),
                        ).asUpdate,
                    )

                val endringerLagret =
                    avklaring.endringer.map { endring ->
                        tx.run(
                            queryOf(
                                // language=PostgreSQL
                                """
                                INSERT INTO avklaring_endring (endring_id, avklaring_id, endret, type, saksbehandler)
                                VALUES (:endring_id, :avklaring_id, :endret, :endring_type, :saksbehandler)
                                ON CONFLICT DO NOTHING
                                """.trimIndent(),
                                mapOf(
                                    "endring_id" to endring.id,
                                    "avklaring_id" to avklaring.id,
                                    "endret" to endring.endret,
                                    "endring_type" to
                                        when (endring) {
                                            is Avklaring.Endring.UnderBehandling -> "UnderBehandling"
                                            is Avklaring.Endring.Avklart -> "Avklart"
                                            is Avklaring.Endring.Avbrutt -> "Avbrutt"
                                        },
                                    "saksbehandler" to
                                        when (endring) {
                                            is Avklaring.Endring.Avklart -> endring.saksbehandler
                                            else -> null
                                        },
                                ),
                            ).asUpdate,
                        )
                    }

                if (lagret != 0) nyeAvklaringer.add(avklaring)
                // if (endringerLagret.any { it == 1 }) TODO("Avklaringen er endret")
            }
        }

        /** TODO
         * 1. [X] Lagre avklaring
         * 2. [X] Rehydrere avklaringer
         * 3. [X] Lage AvkaringIkkeRelevantHendelse - når en avklaring er ikke relevant
         * 4. [X] Sende AvkaringIkkeRelevantHendelse ned modellen til Avklaringer
         * 5. [X] Lage AvkaringIkkeRelevantMottak
         * 6. Skrive om dp-manuell-behandling til å lukke avklaringer
         * 7. Fjerne AvklaringManuellBehandling i Behandling og heller sjekke om det er åpne avklaringer
         */

        nyeAvklaringer.forEach {
            emitNyAvklaring(
                behandling.behandler.ident,
                behandling.toSpesifikkKontekst(),
                it,
            )
        }
    }

    private fun emitNyAvklaring(
        ident: String,
        toSpesifikkKontekst: Behandling.BehandlingKontekst,
        avklaring: Avklaring,
    ) {
        observatører.forEach {
            it.nyAvklaring(
                NyAvklaringHendelse(
                    ident,
                    toSpesifikkKontekst,
                    avklaring,
                ),
            )
        }
    }
}

interface AvklaringRepositoryObserver {
    fun nyAvklaring(nyAvklaringHendelse: NyAvklaringHendelse)

    data class NyAvklaringHendelse(
        val ident: String,
        val kontekst: Behandling.BehandlingKontekst,
        val avklaring: Avklaring,
    )

    fun endretAvklaring(endretAvklaringHendelse: EndretAvklaringHendelse)

    data class EndretAvklaringHendelse(
        val ident: String,
        val kontekst: Behandling.BehandlingKontekst,
        val avklaring: Avklaring,
    )
}
