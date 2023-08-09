package no.nav.dagpenger.vedtak.mediator.persistens

import io.ktor.utils.io.core.use
import kotliquery.TransactionalSession
import kotliquery.queryOf
import kotliquery.sessionOf
import mu.KotlinLogging
import no.nav.dagpenger.vedtak.iverksetting.mediator.mottak.DagpengerInnvilgetMessage
import no.nav.dagpenger.vedtak.iverksetting.mediator.mottak.IverksattHendelseMessage
import no.nav.dagpenger.vedtak.iverksetting.mediator.mottak.UtbetalingsvedtakFattetHendelseMessage
import no.nav.dagpenger.vedtak.mediator.melding.HendelseMessage
import no.nav.dagpenger.vedtak.mediator.melding.HendelseRepository
import no.nav.dagpenger.vedtak.mediator.mottak.RapporteringBehandletHendelseMessage
import no.nav.dagpenger.vedtak.mediator.mottak.SøknadBehandletHendelseMessage
import org.postgresql.util.PGobject
import java.util.UUID
import javax.sql.DataSource

internal class PostgresHendelseRepository(private val dataSource: DataSource) : HendelseRepository {

    override fun lagreMelding(hendelseMessage: HendelseMessage, ident: String, id: UUID, toJson: String) {
        val hendelseType = hendelseType(hendelseMessage) ?: return

        sessionOf(dataSource).use { session ->
            session.transaction { transactionalSession: TransactionalSession ->
                transactionalSession.run(
                    queryOf(
                        //language=PostgreSQL
                        statement = """
                            INSERT INTO hendelse
                                (hendelse_id, hendelse_type, ident, melding, endret)
                            VALUES
                                (:hendelse_id, :hendelse_type, :ident, :melding, now())
                            ON CONFLICT DO NOTHING
                        """.trimIndent(),
                        paramMap = mapOf(
                            "hendelse_id" to id,
                            "hendelse_type" to hendelseType.name,
                            "ident" to ident,
                            "melding" to PGobject().apply {
                                type = "json"
                                value = toJson
                            },
                        ),
                    ).asUpdate,
                )
            }
        }
    }

    override fun markerSomBehandlet(hendelseId: UUID) = sessionOf(dataSource).use { session ->
        session.run(
            queryOf(
                //language=PostgreSQL
                statement = """
                    UPDATE hendelse
                    SET behandlet_tidspunkt=now()
                    WHERE hendelse_id = :hendelse_id
                      AND behandlet_tidspunkt IS NULL
                """.trimIndent(),
                paramMap = mapOf("hendelse_id" to hendelseId),
            ).asUpdate,
        )
    }

    override fun erBehandlet(hendelseId: UUID): Boolean = sessionOf(dataSource).use { session ->
        session.run(
            queryOf(
                //language=PostgreSQL
                statement = """
                    SELECT behandlet_tidspunkt FROM hendelse WHERE hendelse_id = :hendelse_id
                """.trimIndent(),
                paramMap = mapOf("hendelse_id" to hendelseId),
            ).map { rad -> rad.localDateTimeOrNull("behandlet_tidspunkt") }.asSingle,
        ) != null
    }

    private fun hendelseType(hendelseMessage: HendelseMessage): HendelseTypeDTO? {
        return when (hendelseMessage) {
            is SøknadBehandletHendelseMessage -> HendelseTypeDTO.SØKNAD_BEHANDLET
            is RapporteringBehandletHendelseMessage -> HendelseTypeDTO.RAPPORTERING_BEHANDLET
            is IverksattHendelseMessage -> HendelseTypeDTO.IVERKSATT
            is UtbetalingsvedtakFattetHendelseMessage -> HendelseTypeDTO.UTBETALING_VEDTAK_FATTET
            is DagpengerInnvilgetMessage -> HendelseTypeDTO.HOVEDRETTIGHET_VEDTAK_FATTET
            else -> null.also {
                logger.warn { "ukjent meldingstype ${hendelseMessage::class.simpleName}: melding lagres ikke" }
            }
        }
    }

    companion object {
        private val logger = KotlinLogging.logger { }
    }
}

enum class HendelseTypeDTO {
    SØKNAD_BEHANDLET,
    RAPPORTERING_BEHANDLET,
    IVERKSATT,
    UTBETALING_VEDTAK_FATTET,
    HOVEDRETTIGHET_VEDTAK_FATTET,

    @Deprecated("Er ikke i bruk lenger - men finnes instanser i databasen")
    VEDTAK_FATTET,
}
