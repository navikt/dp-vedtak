package no.nav.dagpenger.vedtak.mediator.persistens

import java.util.UUID
import javax.sql.DataSource
import kotliquery.Row
import kotliquery.TransactionalSession
import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import no.nav.dagpenger.vedtak.mediator.melding.HendelseMessage
import no.nav.dagpenger.vedtak.mediator.melding.HendelseRepository
import no.nav.dagpenger.vedtak.modell.Person
import no.nav.dagpenger.vedtak.modell.PersonIdentifikator
import no.nav.dagpenger.vedtak.modell.PersonIdentifikator.Companion.tilPersonIdentfikator
import no.nav.dagpenger.vedtak.modell.hendelser.DagpengerInnvilgetHendelse
import no.nav.dagpenger.vedtak.modell.hendelser.Hendelse
import no.nav.helse.rapids_rivers.JsonMessage

internal class PostgresHendelseRepository( private val dataSource: DataSource): HendelseRepository {
    override fun lagreMelding(hendelseMessage: HendelseMessage, ident: String, id: UUID, toJson: String) {
        using(sessionOf(dataSource)) { session ->
            session.transaction { transactionalSession: TransactionalSession ->
                transactionalSession.run(
                    queryOf(
                        //language=PostgreSQL
                        statement =
                        """
                            INSERT INTO hendlse
                                (hendelse_id, hendelse_type, ident, status, melding, endret)
                            VALUES
                                (:hendelse_id, :hendelse_type, :ident, :status, :melding, now())
                            ON CONFLICT DO NOTHING
                        """.trimIndent(),
                        paramMap = mapOf(
                            "hendelse_id" to id,
                            "hendelse_type" to hendelseMessage.tracinginfo().get("event_name"),
                            "ident" to ident,
                            "status" to MeldingStatus.MOTTATT,
                            "melding" to toJson,
                        )
                    ).asUpdate
                )
            }
        }
    }


    override fun markerSomBehandlet(meldingId: UUID) {
        TODO("Not yet implemented")
    }

    override fun hentMottatte(): List<HendelseMessage> {
      return using(sessionOf(dataSource)) {session ->
          session.run(
              queryOf(
                  //language=PostgreSQL
                  statement = """
                      SELECT *
                      FROM hendelse
                      WHERE status = :status
                      """.trimIndent(),
                  paramMap = mapOf(
                      "status" to MeldingStatus.MOTTATT.name)),
          ).map { rad ->
              HendelseMessage(JsonMessage(rad.string("melding"), null ))

          }


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
    DagpengerAvslåttHendelse
}