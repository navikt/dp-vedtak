package no.nav.dagpenger.vedtak.iverksetting.mediator.persistens

import kotliquery.Query
import kotliquery.Session
import kotliquery.queryOf
import kotliquery.sessionOf
import no.nav.dagpenger.aktivitetslogg.Aktivitet
import no.nav.dagpenger.aktivitetslogg.Aktivitetslogg
import no.nav.dagpenger.vedtak.aktivitetslogg
import no.nav.dagpenger.vedtak.iverksetting.Iverksetting
import no.nav.dagpenger.vedtak.iverksetting.IverksettingBehov
import no.nav.dagpenger.vedtak.iverksetting.IverksettingVisitor
import no.nav.dagpenger.vedtak.mediator.persistens.AktivitetsloggMapper
import no.nav.dagpenger.vedtak.mediator.persistens.BehovTypeMapper
import no.nav.dagpenger.vedtak.modell.PersonIdentifikator
import no.nav.dagpenger.vedtak.modell.PersonIdentifikator.Companion.tilPersonIdentfikator
import no.nav.dagpenger.vedtak.objectMapper
import org.postgresql.util.PGobject
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
                    val iverksettingId = UUID.fromString(row.string("id"))
                    Iverksetting.rehydrer(
                        id = iverksettingId,
                        personIdentifikator = row.string("ident").tilPersonIdentfikator(),
                        vedtakId = UUID.fromString(row.string("vedtak_id")),
                        tilstand = when (TilstandDTO.valueOf(row.string("tilstand"))) {
                            TilstandDTO.Mottatt -> Iverksetting.Mottatt
                            TilstandDTO.AvventerIverksetting -> Iverksetting.AvventerIverksetting
                            TilstandDTO.Iverksatt -> Iverksetting.Iverksatt
                        },
                        aktivitetslogg = session.hentAktivitetslogg(iverksettingId)
                            ?.konverterTilAktivitetslogg(IverksettingBehovTypeMapper)
                            ?: throw RuntimeException("Iverksetting uten aktivitetslogg!"),
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

    private fun Session.hentAktivitetslogg(iverksettingId: UUID) = this.run(
        queryOf(
            //language=PostgreSQL
            statement = """
            SELECT data FROM iverksetting_aktivitetslogg WHERE iverksetting_id = :iverksetting_id
            """.trimIndent(),
            paramMap = mapOf(
                "iverksetting_id" to iverksettingId,
            ),
        ).map { rad ->
            rad.binaryStream("data").aktivitetslogg()
        }.asSingle,
    )

    private object IverksettingBehovTypeMapper : BehovTypeMapper {

        enum class IverksettingBehovDto {
            Iverksett,
        }

        override fun map(behovNavn: String?): Aktivitet.Behov.Behovtype {
            val behov = requireNotNull(behovNavn) { "Forventer at behov navn er satt" }
            return when (IverksettingBehovDto.valueOf(behov)) {
                IverksettingBehovDto.Iverksett -> IverksettingBehov.Iverksett
            }
        }
    }

    private class PopulerQueries(iverksetting: Iverksetting) : IverksettingVisitor {
        var iverksettingId: UUID? = null
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
            iverksettingId = id
            queries.add(

                queryOf(
                    //language=PostgreSQL
                    statement =
                    """
                    INSERT INTO iverksetting (id, vedtak_id, person_id, tilstand, endret)
                    VALUES (:id, :vedtak_id, (SELECT id FROM person WHERE ident = :ident), :tilstand, :endret)
                    ON CONFLICT (id) DO UPDATE SET tilstand = :tilstand, endret = :endret
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

        override fun preVisitAktivitetslogg(aktivitetslogg: Aktivitetslogg) {
            this.queries.add(
                queryOf(
                    //language=PostgreSQL
                    statement = """
                    INSERT INTO iverksetting_aktivitetslogg (iverksetting_id, data)
                    VALUES (:iverksetting_id, :data)
                    ON CONFLICT (iverksetting_id) DO UPDATE SET data = :data
                    """.trimIndent(),
                    paramMap = mapOf(
                        "iverksetting_id" to iverksettingId,
                        "data" to PGobject().apply {
                            type = "json"
                            value = objectMapper.writeValueAsString(AktivitetsloggMapper(aktivitetslogg).toMap())
                        },

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
