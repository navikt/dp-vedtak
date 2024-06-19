package no.nav.dagpenger.behandling.mediator.repository

import kotliquery.Session
import kotliquery.queryOf
import kotliquery.sessionOf
import mu.KotlinLogging
import no.nav.dagpenger.avklaring.Avklaring
import no.nav.dagpenger.avklaring.Avklaringkode
import no.nav.dagpenger.behandling.db.PostgresDataSourceBuilder.dataSource
import no.nav.dagpenger.behandling.modell.Behandling
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import java.util.UUID

internal class AvklaringRepositoryPostgres(
    private val rapid: MessageContext,
) : AvklaringRepository {
    override fun lagreAvklaringer(
        behandling: Behandling,
        unitOfWork: UnitOfWork<*>,
    ) {
        lagre(behandling, unitOfWork as PostgresUnitOfWork)
    }

    override fun hentAvklaringer(behandlingId: UUID): List<Avklaring> {
        sessionOf(dataSource).use { session ->
            return session.run(
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
                        historikk =
                            hentEndringer(row.uuid("id"), session).toMutableList(),
                    )
                }.asList,
            )
        }
    }

    private enum class EndringType {
        UnderBehandling,
        Avklart,
        Avbrutt,
    }

    private fun hentEndringer(
        uuid: UUID,
        session: Session,
    ): List<Avklaring.Endring> =
        session
            .run(
                queryOf(
                    // language=PostgreSQL
                    """
                    SELECT * FROM avklaring_endring WHERE avklaring_id = :avklaring_id
                    """.trimIndent(),
                    mapOf("avklaring_id" to uuid),
                ).map {
                    val endret = it.localDateTime("endret")
                    when (EndringType.valueOf(it.string("type"))) {
                        EndringType.UnderBehandling -> Avklaring.Endring.UnderBehandling(endret)
                        EndringType.Avklart -> Avklaring.Endring.Avklart(it.string("saksbehandler"), endret)
                        EndringType.Avbrutt -> Avklaring.Endring.Avbrutt(endret)
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

                avklaring.endringer.forEach {
                    tx.run(
                        queryOf(
                            // language=PostgreSQL
                            """
                            INSERT INTO avklaring_endring (avklaring_id, endret, type, saksbehandler)
                            VALUES (:avklaring_id, :endret, :endring_type, :saksbehandler)
                            """.trimIndent(),
                            mapOf(
                                "avklaring_id" to avklaring.id,
                                "endret" to it.endret,
                                "endring_type" to
                                    when (it) {
                                        is Avklaring.Endring.UnderBehandling -> "UnderBehandling"
                                        is Avklaring.Endring.Avklart -> "Avklart"
                                        is Avklaring.Endring.Avbrutt -> "Avbrutt"
                                    },
                                "saksbehandler" to
                                    when (it) {
                                        is Avklaring.Endring.Avklart -> it.saksbehandler
                                        else -> null
                                    },
                            ),
                        ).asUpdate,
                    )
                }

                if (lagret != 0) nyeAvklaringer.add(avklaring)
            }
        }

        /** TODO
         * 1. [X] Lagre avklaring
         * 2. [X] Rehydrere avklaringer
         * 3. Lage AvklaringAvklartHendelse - når en avklaring er avklart
         * 4. Sende AvklaringAvklartHendelse ned modellen til Avklaringer
         * 5. Lage AvklaringAvklartMottak
         * 6. Skrive om dp-manuell-behandling til å lukke avklaringer
         * 7. Fjerne AvklaringManuellBehandling i Behandling og heller sjekke om det er åpne avklaringer
         */

        nyeAvklaringer.forEach {
            publiser(
                behandling.behandler.ident,
                behandling.toSpesifikkKontekst(),
                it,
            )
        }
    }

    private fun publiser(
        ident: String,
        kontekst: Behandling.BehandlingKontekst,
        avklaring: Avklaring,
    ) {
        rapid.publish(
            ident,
            JsonMessage
                .newMessage(
                    "NyAvklaring",
                    mapOf<String, Any>(
                        "ident" to ident,
                        "avklaringId" to avklaring.id,
                        "kode" to avklaring.kode.kode,
                    ) + kontekst.kontekstMap,
                ).toJson(),
        )

        logger.info { "Publisert NyAvklaring for avklaringId=${avklaring.id}" }
    }

    private companion object {
        private val logger = KotlinLogging.logger {}
    }
}
