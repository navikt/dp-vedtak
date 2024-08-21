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
    private val kildeRespository: KildeRepository = KildeRepository(),
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
                    SELECT *
                    FROM avklaring
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
                    EndringType.Avklart ->
                        Avklaring.Endring.Avklart(
                            id,
                            it.uuidOrNull("kilde_id")?.let { kildeId ->
                                kildeRespository.hentKilde(kildeId)
                            },
                            endret,
                        )
                    EndringType.Avbrutt -> Avklaring.Endring.Avbrutt(id, endret)
                }
            }.asList,
        )

    private fun lagre(
        behandling: Behandling,
        unitOfWork: PostgresUnitOfWork,
    ) {
        val avklaringer = behandling.avklaringer()
        val nyeAvklaringer = mutableListOf<Avklaring>()

        unitOfWork.inTransaction { tx ->
            avklaringer.forEach { avklaring ->
                val avklaringskode = avklaring.kode
                val lagret =
                    tx.run(
                        queryOf(
                            // language=PostgreSQL
                            """
                            INSERT INTO avklaring (id, behandling_id, kode, tittel, beskrivelse, kan_kvitteres)
                            VALUES (:avklaring_id, :behandling_id, :kode, :tittel, :beskrivelse, :kanKvitteres)
                            ON CONFLICT (id) DO NOTHING
                            """.trimIndent(),
                            mapOf(
                                "avklaring_id" to avklaring.id,
                                "behandling_id" to behandling.behandlingId,
                                "kode" to avklaringskode.kode,
                                "tittel" to avklaringskode.tittel,
                                "beskrivelse" to avklaringskode.beskrivelse,
                                "kanKvitteres" to avklaringskode.kanKvitteres,
                            ),
                        ).asUpdate,
                    )

                val endringerLagret =
                    avklaring.endringer.map { endring ->
                        val kildeId =
                            when (endring) {
                                is Avklaring.Endring.Avklart -> {
                                    if (endring.avklartAv != null) {
                                        kildeRespository.lagreKilde(endring.avklartAv!!, tx)
                                        endring.avklartAv!!.id
                                    } else {
                                        null
                                    }
                                }
                                else -> null
                            }

                        tx.run(
                            queryOf(
                                // language=PostgreSQL
                                """
                                INSERT INTO avklaring_endring (endring_id, avklaring_id, endret, type, kilde_id)
                                VALUES (:endring_id, :avklaring_id, :endret, :endring_type, :kilde_id)
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
                                    "kilde_id" to kildeId,
                                ),
                            ).asUpdate,
                        )
                    }

                if (lagret != 0) nyeAvklaringer.add(avklaring)
                // if (endringerLagret.any { it == 1 }) TODO("Avklaringen er endret")
            }
        }
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
