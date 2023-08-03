package no.nav.dagpenger.vedtak.iverksetting.mediator.persistens

import kotliquery.Query
import kotliquery.queryOf
import kotliquery.sessionOf
import no.nav.dagpenger.vedtak.iverksetting.Iverksetting
import no.nav.dagpenger.vedtak.iverksetting.IverksettingVisitor
import no.nav.dagpenger.vedtak.modell.PersonIdentifikator
import no.nav.dagpenger.vedtak.modell.PersonIdentifikator.Companion.tilPersonIdentfikator
import java.time.LocalDateTime
import java.util.UUID
import javax.sql.DataSource

internal class PostgresIverksettingRepository(private val dataSource: DataSource) : IverksettingRepository {
    override fun hent(vedtakId: UUID): Iverksetting? {
        return sessionOf(dataSource).use { session ->
            session.run(
                queryOf(
                    //language=PostgreSQL
                    statement = """
                    SELECT iverksetting.id, person.ident, iverksetting.vedtak_id, iverksetting.tilstand
                    FROM iverksetting
                    JOIN person ON person.id = iverksetting.person_id
                    WHERE vedtak_id = :vedtakId
                    """.trimIndent(),
                    paramMap = mapOf("vedtakId" to vedtakId),
                ).map { row ->
                    Iverksetting.rehydrer(
                        id = UUID.fromString(row.string("id")),
                        personIdentifikator = row.string("ident").tilPersonIdentfikator(),
                        vedtakId = UUID.fromString(row.string("vedtak_id")),
                        tilstand = when (TilstandDTO.valueOf(row.string("tilstand"))) {
                            TilstandDTO.Mottatt -> Iverksetting.Mottatt
                            TilstandDTO.AvventerIverksetting -> Iverksetting.AvventerIverksetting
                            TilstandDTO.Iverksatt -> Iverksetting.Iverksatt
                        },
                    )
                }.asSingle,
            )
        }
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

enum class TilstandDTO {
    Mottatt,
    AvventerIverksetting,
    Iverksatt,
}
