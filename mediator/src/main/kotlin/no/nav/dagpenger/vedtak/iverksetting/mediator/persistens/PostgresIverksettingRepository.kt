package no.nav.dagpenger.vedtak.iverksetting.mediator.persistens

import kotliquery.Query
import kotliquery.queryOf
import kotliquery.sessionOf
import no.nav.dagpenger.vedtak.iverksetting.Iverksetting
import no.nav.dagpenger.vedtak.iverksetting.IverksettingVisitor
import no.nav.dagpenger.vedtak.modell.PersonIdentifikator
import java.time.LocalDateTime
import java.util.UUID
import javax.sql.DataSource

internal class PostgresIverksettingRepository(private val dataSource: DataSource) : IverksettingRepository {
    override fun hent(vedtakId: UUID): Iverksetting? {
        TODO("Not yet implemented")
    }

    override fun lagre(iverksetting: Iverksetting) {
        val populerQueries = PopulerQueries(iverksetting)
        sessionOf(dataSource).use { session ->
            session.transaction { transactionalSession ->
                populerQueries.queries.forEach {
                    transactionalSession.run(it.asUpdate)
                }
            }
        }
    }

    private class PopulerQueries(iverksetting: Iverksetting) : IverksettingVisitor {

        val queries = mutableListOf<Query>()

        init {
            iverksetting.accept(this)
        }

        override fun visitIverksetting(
            id: UUID,
            vedtakId: UUID,
            personIdent: PersonIdentifikator,
            tilstand: Iverksetting.Tilstand,
        ) {
            queries.add(

                queryOf(
                    //language=PostgreSQL
                    statement =
                    """
                    INSERT INTO iverksetting (id, vedtak_id, person_id, tilstand, endret)
                    VALUES (:id, :vedtak_id, (SELECT id FROM person WHERE ident = :ident), :tilstand, :endret)
                    """.trimIndent(),
                    paramMap = mapOf(
                        "id" to id,
                        "vedtak_id" to vedtakId,
                        "ident" to personIdent.identifikator(),
                        "tilstand" to tilstand.tilstandNavn.name,
                        "endret" to LocalDateTime.now(),
                    ),
                ),

            )
        }
    }
}
