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
import no.nav.dagpenger.vedtak.modell.entitet.Beløp.Companion.beløp
import no.nav.dagpenger.vedtak.modell.entitet.Stønadsdager
import no.nav.dagpenger.vedtak.modell.entitet.Timer
import no.nav.dagpenger.vedtak.modell.entitet.Timer.Companion.timer
import no.nav.dagpenger.vedtak.modell.rapportering.Dag
import no.nav.dagpenger.vedtak.modell.vedtak.Rammevedtak
import no.nav.dagpenger.vedtak.modell.vedtak.fakta.AntallStønadsdager
import no.nav.dagpenger.vedtak.modell.vedtak.fakta.Dagsats
import no.nav.dagpenger.vedtak.modell.vedtak.fakta.Faktum
import no.nav.dagpenger.vedtak.modell.vedtak.fakta.VanligArbeidstidPerDag
import no.nav.dagpenger.vedtak.modell.visitor.PersonVisitor
import java.math.BigDecimal
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
                     SELECT *
                     FROM person
                     WHERE ident = :ident
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
                val populerQueries = PopulerQueries(person, dbPersonId, transactionalSession)
                populerQueries.queries.forEach {
                    transactionalSession.run(it.asUpdate)
                }
            }
        }
    }
}

private class PopulerQueries(
    person: Person,
    private val dbPersonId: Long,
    private val session: Session,
) : PersonVisitor {

    val queries = mutableListOf<Query>()
    private var vedtakId: UUID? = null
    private var rapporteringDbId: Long? = null

    init {
        person.accept(this)
    }

    override fun preVisitRapporteringsperiode(rapporteringsperiodeId: UUID, fom: LocalDate, tom: LocalDate) {
        this.rapporteringDbId = session.hentRapportering(rapporteringsperiodeId)
            ?: session.opprettRapportering(dbPersonId, rapporteringsperiodeId, fom, tom)
            ?: throw RuntimeException("Vi kunne ikke lagre rapporteringsperiode med uuid $rapporteringsperiodeId. Noe er veldig galt!")
    }

    override fun visitdag(dag: Dag) {
    }

    override fun postVisitRapporteringsperiode(rapporteringsperiode: UUID, fom: LocalDate, tom: LocalDate) {
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
                    INSERT INTO vedtak
                        (id, person_id, behandling_id, virkningsdato, vedtakstidspunkt)
                    VALUES (:id, :person_id, :behandling_id, :virkningsdato, :vedtakstidspunkt)
                    ON CONFLICT DO NOTHING
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
            fakta = this.hentFakta(vedtakId = rad.uuid("id"))?.fakta ?: emptyList(),
            rettigheter = emptyList(),
        )
    }.asList,
)

private fun Session.hentFakta(vedtakId: UUID) = this.run(
    queryOf(
        //language=PostgreSQL
        statement = """
                SELECT dagsats.beløp                          AS dagsats,
                       stønadsperiode.antall_dager            AS stønadsperiode,
                       vanlig_arbeidstid.antall_timer_per_dag AS vanlig_arbeidstid_per_dag
                FROM vedtak
                         LEFT JOIN dagsats ON vedtak.id = dagsats.vedtak_id
                         LEFT JOIN stønadsperiode ON vedtak.id = stønadsperiode.vedtak_id
                         LEFT JOIN vanlig_arbeidstid ON vedtak.id = vanlig_arbeidstid.vedtak_id
                WHERE vedtak.id = :vedtakId  
        """.trimMargin(),
        paramMap = mapOf("vedtakId" to vedtakId),
    ).map { rad ->
        FaktaRad(
            dagsats = rad.bigDecimalOrNull("dagsats"),
            stønadsperiode = rad.intOrNull("stønadsperiode"),
            vanligArbeidstidPerDag = rad.bigDecimalOrNull("vanlig_arbeidstid_per_dag"),
        )
    }.asSingle,
)

private fun Session.opprettPerson(ident: String) = this.run(
    queryOf(
        //language=PostgreSQL
        statement = """INSERT INTO person (ident) VALUES (:ident) ON CONFLICT DO NOTHING RETURNING id""",
        paramMap = mapOf("ident" to ident),
    ).map { rad -> rad.long("id") }.asSingle,
)

private fun Session.hentRapportering(rapporteringsperiodeId: UUID) = this.run(

    queryOf(
        statement = //language=PostgreSQL
        """SELECT id FROM rapporteringsperiode WHERE uuid = :uuid""".trimIndent(),
        paramMap = mapOf("uuid" to rapporteringsperiodeId),
    ).map { rad ->
        rad.long("id")
    }.asSingle,
)

private fun Session.opprettRapportering(
    dbPersonId: Long,
    rapporteringsperiodeId: UUID,
    fom: LocalDate,
    tom: LocalDate,
) = this.run(

    queryOf(

        statement = //language=PostgreSQL
        """ 
                INSERT INTO rapporteringsperiode(uuid, person_id, fom, tom, endret)
                VALUES (:uuid, :personId, :fom, :tom, now()) RETURNING id;
        """.trimIndent(),
        paramMap = mapOf("uuid" to rapporteringsperiodeId, "personId" to dbPersonId, "fom" to fom, "tom" to tom),
    ).map { rad ->
        rad.long("id")
    }.asSingle,
)

private data class FaktaRad(
    val dagsats: BigDecimal?,
    val stønadsperiode: Int?,
    val vanligArbeidstidPerDag: BigDecimal?,
) {

    val fakta = mutableListOf<Faktum<*>>()

    init {
        if (dagsats != null) {
            fakta.add(Dagsats(dagsats.beløp))
        }
        if (stønadsperiode != null) {
            fakta.add(AntallStønadsdager(Stønadsdager(stønadsperiode)))
        }

        if (vanligArbeidstidPerDag != null) {
            fakta.add(VanligArbeidstidPerDag(vanligArbeidstidPerDag.timer))
        }
    }
}
