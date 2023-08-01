package no.nav.dagpenger.vedtak.mediator.persistens

import kotliquery.TransactionalSession
import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import no.nav.dagpenger.vedtak.mediator.melding.HendelseMessage
import no.nav.dagpenger.vedtak.mediator.melding.HendelseRepository
import no.nav.dagpenger.vedtak.mediator.mottak.SøknadBehandletHendelseMessage
import no.nav.dagpenger.vedtak.objectMapper
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageProblems
import org.postgresql.util.PGobject
import java.util.UUID
import javax.sql.DataSource

internal class PostgresHendelseRepository(private val dataSource: DataSource) : HendelseRepository {
    override fun lagreMelding(hendelseMessage: HendelseMessage, ident: String, id: UUID, toJson: String) {
        using(sessionOf(dataSource)) { session ->
            session.transaction { transactionalSession: TransactionalSession ->
                transactionalSession.run(
                    queryOf(
                        //language=PostgreSQL
                        statement =
                        """
                            INSERT INTO hendelse
                                (hendelse_id, hendelse_type, ident, status, melding, endret)
                            VALUES
                                (:hendelse_id, :hendelse_type, :ident, :status, :melding, now())
                            ON CONFLICT DO NOTHING
                        """.trimIndent(),
                        paramMap = mapOf(
                            "hendelse_id" to id,
                            "hendelse_type" to "søknad_behandlet_hendelse",
                            "ident" to ident,
                            "status" to MeldingStatus.MOTTATT.name,
                            "melding" to PGobject().apply {
                                type = "json"
                                value = objectMapper.writeValueAsString(toJson)
                            },
                        ),
                    ).asUpdate,
                )
            }
        }
    }

    override fun markerSomBehandlet(meldingId: UUID) {
        TODO("Not yet implemented")
    }

    override fun hentMottatte(): List<HendelseMessage> {
        return using(sessionOf(dataSource)) { session ->
            session.run(
                queryOf(
                    //language=PostgreSQL
                    statement = """
                      SELECT *
                      FROM hendelse
                      WHERE status = :status
                    """.trimIndent(),
                    paramMap = mapOf("status" to MeldingStatus.MOTTATT.name),
                ).map { rad ->
                    val melding = rad.string("melding")
                    /*when (HendelseTypeDTO.valueOf(rad.string("hendelse_type"))) {
                        HendelseTypeDTO.DagpengerInnvilgetHendelse -> */
                    SøknadBehandletHendelseMessage(
                        JsonMessage(
                            melding,
                            MessageProblems(melding),
                        ),
                    )
                    // HendelseTypeDTO.DagpengerAvslåttHendelse -> TODO()
                    // }
                }.asList,
            )
        }
    }

    override fun hentBehandlede(): List<HendelseMessage> {
        TODO("Not yet implemented")
    }

    private enum class MeldingStatus {
        MOTTATT, BEHANDLET
    }
}

enum class HendelseTypeDTO {
    DagpengerInnvilgetHendelse,
    DagpengerAvslåttHendelse,
}
