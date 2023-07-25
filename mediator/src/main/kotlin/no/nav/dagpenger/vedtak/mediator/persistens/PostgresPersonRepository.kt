package no.nav.dagpenger.vedtak.mediator.persistens

import kotliquery.Query
import kotliquery.Session
import kotliquery.TransactionalSession
import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import no.nav.dagpenger.vedtak.modell.Person
import no.nav.dagpenger.vedtak.modell.PersonIdentifikator
import no.nav.dagpenger.vedtak.modell.PersonIdentifikator.Companion.tilPersonIdentfikator
import no.nav.dagpenger.vedtak.modell.vedtak.Rammevedtak
import no.nav.dagpenger.vedtak.modell.vedtak.VedtakHistorikk
import no.nav.dagpenger.vedtak.modell.visitor.PersonVisitor
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID
import javax.sql.DataSource

class PostgresPersonRepository(private val dataSource: DataSource) : PersonRepository {
    override fun hent(ident: PersonIdentifikator): Person? {
        return using(sessionOf(dataSource)) { session ->
            session.run(
                queryOf(
                    //language=PostgreSQL
                    statement = """
                     SELECT * FROM person WHERE ident = :ident
                    """.trimIndent(),
                    paramMap = mapOf("ident" to ident.identifikator()),
                ).map { row ->
                    Person.rehydrer(
                        ident = row.string("ident").tilPersonIdentfikator(),
                        vedtakHistorikk = VedtakHistorikk(session.hentVedtak(row.long("id"))),
                    )
                }.asSingle,
            )
        }
    }

    override fun lagre(person: Person) {
        using(sessionOf(dataSource)) { session ->
            session.transaction { transactionalSession: TransactionalSession ->
                val dbPersonId = transactionalSession.hentPerson(person.ident().identifikator())
                    ?: transactionalSession.opprettPerson(person.ident().identifikator())
                    ?: throw RuntimeException("Kunne ikke finne eller opprette person")
                val populerQueries = PopulerQueries(person, dbPersonId)
                populerQueries.queries.forEach {
                    transactionalSession.run(it.asUpdate)
                }
            }
        }
    }

    private fun Session.hentPerson(ident: String) = this.run(
        queryOf(
            //language=PostgreSQL
            statement = """SELECT id FROM person WHERE ident = :ident""",
            paramMap = mapOf("ident" to ident),
        ).map { rad -> rad.longOrNull("id") }.asSingle,
    )

    private fun Session.hentVedtak(personId: Long) = this.run(
        queryOf(
            //language=PostgreSQL
            statement = """ SELECT * FROM vedtak WHERE person_id = :personId """,
            paramMap = mapOf("personId" to personId),
        ).map { rad ->
            Rammevedtak(
                vedtakId = rad.uuid("id"),
                behandlingId = rad.uuid("behandling_id"),
                vedtakstidspunkt = rad.localDateTime("vedtakstidspunkt"),
                virkningsdato = rad.localDate("virkningsdato"),
                fakta = emptyList(),
                rettigheter = emptyList(),
            )
        }.asList,
    )

    private fun Session.opprettPerson(ident: String) = this.run(
        queryOf(
            //language=PostgreSQL
            statement = """INSERT INTO person (ident) VALUES (:ident) ON CONFLICT DO NOTHING RETURNING id""",
            paramMap = mapOf("ident" to ident),
        ).map { rad -> rad.long("id") }.asSingle,
    )
}

class PopulerQueries(person: Person, private val dbPersonId: Long) : PersonVisitor {

    // private val personIdentifikator =
    val queries = mutableListOf<Query>()

    init {
        person.accept(this)
    }

    override fun preVisitVedtak(
        vedtakId: UUID,
        behandlingId: UUID,
        virkningsdato: LocalDate,
        vedtakstidspunkt: LocalDateTime,
    ) {
        queries.add(
            queryOf(
                //language=PostgreSQL
                statement = """
                    |INSERT INTO vedtak 
                    |       (id, person_id, behandling_id, virkningsdato, vedtakstidspunkt) 
                    |VALUES (:id, :person_id, :behandling_id, :virkningsdato, :vedtakstidspunkt )
                    |ON CONFLICT DO NOTHING
                """.trimMargin(),
                paramMap = mapOf(
                    "id" to vedtakId,
                    "person_id" to dbPersonId,
                    "behandling_id" to behandlingId,
                    "virkningsdato" to virkningsdato,
                    "vedtakstidspunkt" to vedtakstidspunkt,
                ),
            ),
        )
    }
}
