package no.nav.dagpenger.behandling.mediator.melding

import io.ktor.utils.io.core.use
import kotliquery.TransactionalSession
import kotliquery.queryOf
import kotliquery.sessionOf
import mu.KotlinLogging
import no.nav.dagpenger.behandling.db.PostgresDataSourceBuilder.dataSource
import no.nav.dagpenger.behandling.mediator.mottak.OpplysningSvarMessage
import no.nav.dagpenger.behandling.mediator.mottak.SøknadInnsendtMessage
import org.postgresql.util.PGobject
import java.util.UUID

internal class PostgresHendelseRepository() : HendelseRepository {
    override fun lagreMelding(
        hendelseMessage: HendelseMessage,
        ident: String,
        id: UUID,
        toJson: String,
    ) {
        val hendelseType = meldingType(hendelseMessage) ?: return

        sessionOf(dataSource).use { session ->
            session.transaction { transactionalSession: TransactionalSession ->
                transactionalSession.run(
                    queryOf(
                        //language=PostgreSQL
                        statement =
                            """
                            INSERT INTO melding
                                (ident, melding_id, melding_type, data, lest_dato)
                            VALUES
                                (:ident, :melding_id, :melding_type, :data, NOW())
                            ON CONFLICT DO NOTHING
                            """.trimIndent(),
                        paramMap =
                            mapOf(
                                "ident" to ident,
                                "melding_id" to id,
                                "melding_type" to hendelseType.name,
                                "data" to
                                    PGobject().apply {
                                        type = "json"
                                        value = toJson
                                    },
                            ),
                    ).asUpdate,
                )
            }
        }
    }

    override fun markerSomBehandlet(meldingId: UUID) =
        sessionOf(dataSource).use { session ->
            session.run(
                queryOf(
                    //language=PostgreSQL
                    statement =
                        """
                        UPDATE melding
                        SET behandlet_tidspunkt=NOW()
                        WHERE melding_id = :melding_id
                          AND behandlet_tidspunkt IS NULL
                        """.trimIndent(),
                    paramMap = mapOf("melding_id" to meldingId),
                ).asUpdate,
            )
        }

    override fun erBehandlet(meldingId: UUID): Boolean =
        sessionOf(dataSource).use { session ->
            session.run(
                queryOf(
                    //language=PostgreSQL
                    statement =
                        """
                        SELECT behandlet_tidspunkt FROM melding WHERE melding_id = :melding_id
                        """.trimIndent(),
                    paramMap = mapOf("melding_id" to meldingId),
                ).map { rad -> rad.localDateTimeOrNull("behandlet_tidspunkt") }.asSingle,
            ) != null
        }

    private fun meldingType(hendelseMessage: HendelseMessage): MeldingTypeDTO? {
        return when (hendelseMessage) {
            is SøknadInnsendtMessage -> MeldingTypeDTO.SØKNAD_INNSENDT
            is OpplysningSvarMessage -> MeldingTypeDTO.OPPLYSNING_SVAR
            else ->
                null.also {
                    logger.warn { "ukjent meldingstype ${hendelseMessage::class.simpleName}: melding lagres ikke" }
                }
        }
    }

    companion object {
        private val logger = KotlinLogging.logger { }
    }
}

private enum class MeldingTypeDTO {
    SØKNAD_INNSENDT,
    OPPLYSNING_SVAR,
}
