package no.nav.dagpenger.behandling.mediator.repository

import kotliquery.queryOf
import kotliquery.sessionOf
import mu.KotlinLogging
import no.nav.dagpenger.avklaring.Avklaring
import no.nav.dagpenger.behandling.db.PostgresDataSourceBuilder.dataSource
import no.nav.dagpenger.behandling.modell.Behandling
import no.nav.dagpenger.regel.Avklaringspunkter
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
        sessionOf(dataSource).use {
            return it.run(
                queryOf(
                    // language=PostgreSQL
                    """
                    SELECT * 
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
                            Avklaringspunkter.valueOf(
                                row.string("avklaring_kode"),
                            ),
                    )
                }.asList,
            )
        }
    }

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
