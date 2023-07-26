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
import no.nav.dagpenger.vedtak.modell.entitet.Beløp
import no.nav.dagpenger.vedtak.modell.entitet.Stønadsdager
import no.nav.dagpenger.vedtak.modell.entitet.Timer
import no.nav.dagpenger.vedtak.modell.vedtak.Rammevedtak
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
                        vedtak = session.hentVedtak(row.long("id")),
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
    private var vedtakId: UUID? = null

    init {
        person.accept(this)
    }

    override fun preVisitVedtak(
        vedtakId: UUID,
        behandlingId: UUID,
        virkningsdato: LocalDate,
        vedtakstidspunkt: LocalDateTime,
    ) {
        this.vedtakId = vedtakId
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

    override fun visitDagsats(beløp: Beløp) {
        queries.add(
            queryOf(
                //language=PostgreSQL
                statement = """
                    INSERT INTO dagsats
                           (vedtak_id, beløp)
                    VALUES (:vedtak_id, :belop)
                    ON CONFLICT DO NOTHING
                """.trimIndent(),
                paramMap = mapOf(
                    "vedtak_id" to vedtakId,
                    "belop" to beløp.reflection { it },
                ),
            ),
        )
    }

    override fun visitVanligArbeidstidPerDag(timer: Timer) {
        queries.add(
            queryOf(
                //language=PostgreSQL
                statement = """
                    INSERT INTO vanlig_arbeidstid
                           (vedtak_id, antall_timer_per_dag)
                    VALUES (:vedtak_id, :timer)
                    ON CONFLICT DO NOTHING
                """.trimIndent(),
                paramMap = mapOf(
                    "vedtak_id" to vedtakId,
                    "timer" to timer.reflection { it },
                ),
            ),
        )
    }

    override fun visitAntallStønadsdager(dager: Stønadsdager) {
        queries.add(
            queryOf(
                //language=PostgreSQL
                statement = """
                    INSERT INTO stønadsperiode
                           (vedtak_id, antall_dager)
                    VALUES (:vedtak_id, :antall_dager)
                    ON CONFLICT DO NOTHING
                """.trimIndent(),
                paramMap = mapOf(
                    "vedtak_id" to vedtakId,
                    "antall_dager" to dager.stønadsdager(),
                ),
            ),
        )
    }

    override fun postVisitVedtak(
        vedtakId: UUID,
        behandlingId: UUID,
        virkningsdato: LocalDate,
        vedtakstidspunkt: LocalDateTime,
    ) {
        this.vedtakId = null
    }
}
