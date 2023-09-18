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
import no.nav.dagpenger.vedtak.modell.Sak
import no.nav.dagpenger.vedtak.modell.SakId
import no.nav.dagpenger.vedtak.modell.entitet.Beløp
import no.nav.dagpenger.vedtak.modell.entitet.Beløp.Companion.beløp
import no.nav.dagpenger.vedtak.modell.entitet.Periode
import no.nav.dagpenger.vedtak.modell.entitet.Stønadsdager
import no.nav.dagpenger.vedtak.modell.entitet.Timer
import no.nav.dagpenger.vedtak.modell.entitet.Timer.Companion.timer
import no.nav.dagpenger.vedtak.modell.rapportering.Aktivitet
import no.nav.dagpenger.vedtak.modell.rapportering.Arbeid
import no.nav.dagpenger.vedtak.modell.rapportering.Ferie
import no.nav.dagpenger.vedtak.modell.rapportering.Rapporteringsdag
import no.nav.dagpenger.vedtak.modell.rapportering.Rapporteringsperiode
import no.nav.dagpenger.vedtak.modell.rapportering.Syk
import no.nav.dagpenger.vedtak.modell.utbetaling.Utbetalingsdag
import no.nav.dagpenger.vedtak.modell.vedtak.Avslag
import no.nav.dagpenger.vedtak.modell.vedtak.Rammevedtak
import no.nav.dagpenger.vedtak.modell.vedtak.Utbetalingsvedtak
import no.nav.dagpenger.vedtak.modell.vedtak.Vedtak
import no.nav.dagpenger.vedtak.modell.vedtak.fakta.AntallStønadsdager
import no.nav.dagpenger.vedtak.modell.vedtak.fakta.Dagsats
import no.nav.dagpenger.vedtak.modell.vedtak.fakta.Faktum
import no.nav.dagpenger.vedtak.modell.vedtak.fakta.VanligArbeidstidPerDag
import no.nav.dagpenger.vedtak.modell.vedtak.rettighet.Ordinær
import no.nav.dagpenger.vedtak.modell.visitor.PersonVisitor
import java.math.BigDecimal
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID
import javax.sql.DataSource

class PostgresPersonRepository(private val dataSource: DataSource) : PersonRepository {
    override fun hent(ident: PersonIdentifikator): Person? {
        var personDbId: Long
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
                    personDbId = row.long("id")
                    Person.rehydrer(
                        ident = row.string("ident").tilPersonIdentfikator(),
                        saker = mutableListOf(),
                        vedtak = session.hentVedtak(personDbId),
                        perioder = session.hentRapporteringsperioder(personDbId),

                    ).also { person ->
                        session.hentSaker(person, personDbId)
                    }
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

    override fun preVisitRapporteringsperiode(rapporteringsperiodeId: UUID, periode: Rapporteringsperiode) {
        this.rapporteringDbId = session.hentRapportering(rapporteringsperiodeId)
            ?: session.opprettRapportering(dbPersonId, rapporteringsperiodeId, periode)
            ?: throw RuntimeException("Kunne ikke lagre rapporteringsperiode med uuid $rapporteringsperiodeId. Noe er veldig galt!")
    }

    override fun visitSak(sakId: SakId) {
        queries.add(
            queryOf(
                //language=PostgreSQL
                statement = """
                    INSERT INTO sak (id, person_id)
                    VALUES (:id, :person_id)
                    ON CONFLICT DO NOTHING
                """.trimIndent(),
                paramMap = mapOf(
                    "id" to sakId,
                    "person_id" to dbPersonId,
                ),
            ),
        )
    }

    override fun visitRapporteringsdag(rapporteringsdag: Rapporteringsdag, aktiviteter: List<Aktivitet>) {
        val arbeidTimer = aktiviteter.firstOrNull { it.type == Aktivitet.AktivitetType.Arbeid }?.timer
        val ferieTimer = aktiviteter.firstOrNull { it.type == Aktivitet.AktivitetType.Ferie }?.timer
        val sykTimer = aktiviteter.firstOrNull { it.type == Aktivitet.AktivitetType.Syk }?.timer
        queries.add(
            queryOf(
                //language=PostgreSQL
                statement = """
                    INSERT INTO dag (rapporteringsperiode_id, dato, syk_timer, arbeid_timer, ferie_timer)
                    VALUES (:rapporteringsperiode_id, :dato, :syk, :arbeid, :ferie)
                    ON CONFLICT DO NOTHING
                """.trimIndent(),
                paramMap = mapOf(
                    "rapporteringsperiode_id" to rapporteringDbId,
                    "dato" to rapporteringsdag.dato(),
                    "syk" to sykTimer?.let { timer -> timer.reflection { it } },
                    "arbeid" to arbeidTimer?.let { timer -> timer.reflection { it } },
                    "ferie" to ferieTimer?.let { timer -> timer.reflection { it } },
                ),
            ),
        )
    }

    override fun postVisitRapporteringsperiode(rapporteringsperiodeId: UUID, periode: Rapporteringsperiode) {
        this.rapporteringDbId = null
    }

    override fun preVisitVedtak(
        vedtakId: UUID,
        sakId: SakId,
        behandlingId: UUID,
        virkningsdato: LocalDate,
        vedtakstidspunkt: LocalDateTime,
        type: Vedtak.VedtakType,
    ) {
        this.vedtakId = vedtakId
        queries.add(
            queryOf(
                //language=PostgreSQL
                statement = """
                    INSERT INTO vedtak
                        (id, person_id, sak_id, behandling_id, virkningsdato, vedtakstidspunkt, "type")
                    VALUES 
                        (:id, :person_id, :sak_id, :behandling_id, :virkningsdato, :vedtakstidspunkt, :type)
                    ON CONFLICT DO NOTHING
                """.trimIndent(),
                paramMap = mapOf(
                    "id" to vedtakId,
                    "person_id" to dbPersonId,
                    "sak_id" to sakId,
                    "behandling_id" to behandlingId,
                    "virkningsdato" to virkningsdato,
                    "vedtakstidspunkt" to vedtakstidspunkt,
                    "type" to type.name,
                ),
            ),
        )
    }

    override fun visitUtbetalingsvedtak(
        vedtakId: UUID,
        utfall: Boolean,
        forbruk: Stønadsdager,
        beløpTilUtbetaling: Beløp,
        utbetalingsdager: List<Utbetalingsdag>,
    ) {
        queries.add(
            queryOf(
                //language=PostgreSQL
                statement = """
                INSERT INTO utbetaling
                    (vedtak_id, utfall, forbruk)
                VALUES 
                    (:vedtak_id, :utfall, :forbruk)
                ON CONFLICT DO NOTHING 
                """.trimIndent(),
                paramMap = mapOf(
                    "vedtak_id" to vedtakId,
                    "utfall" to utfall,
                    "forbruk" to forbruk.stønadsdager(),
                ),
            ),
        )

        utbetalingsdager.forEach { dag ->
            queries.add(
                queryOf(
                    //language=PostgreSQL
                    statement = """
                        INSERT INTO utbetalingsdag
                            (vedtak_id, dato, beløp)
                        VALUES (:vedtak_id, :dato, :belop)
                        ON CONFLICT DO NOTHING 
                    """.trimIndent(),
                    paramMap = mapOf(
                        "vedtak_id" to vedtakId,
                        "dato" to dag.dato,
                        "belop" to dag.beløp.reflection { it },
                    ),
                ),
            )
        }
    }

    override fun visitDagsats(beløp: Beløp) {
        queries.add(
            queryOf(
                //language=PostgreSQL
                statement = """
                    INSERT INTO dagsats
                        (vedtak_id, beløp)
                    VALUES 
                        (:vedtak_id, :belop)
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
                    VALUES
                        (:vedtak_id, :timer)
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

    override fun visitOrdinær(ordinær: Ordinær) {
        queries.add(
            queryOf(
                //language=PostgreSQL
                statement = """
                    INSERT INTO rettighet
                           (vedtak_id, rettighetstype, utfall)
                    VALUES (:vedtak_id, :rettighetstype, :utfall)
                    ON CONFLICT DO NOTHING
                """.trimIndent(),
                paramMap = mapOf(
                    "vedtak_id" to vedtakId,
                    "rettighetstype" to ordinær.type.name,
                    "utfall" to ordinær.utfall,
                ),
            ),
        )
    }

    override fun postVisitVedtak(
        vedtakId: UUID,
        sakId: SakId,
        behandlingId: UUID,
        virkningsdato: LocalDate,
        vedtakstidspunkt: LocalDateTime,
        type: Vedtak.VedtakType,
    ) {
        this.vedtakId = null
    }
}

private fun Session.hentPerson(ident: String) = this.run(
    queryOf(
        //language=PostgreSQL
        statement = """
            SELECT id 
            FROM person 
            WHERE ident = :ident
        """.trimIndent(),
        paramMap = mapOf("ident" to ident),
    ).map { rad -> rad.longOrNull("id") }.asSingle,
)

private fun Session.hentSaker(person: Person, personDbId: Long) = this.run(
    queryOf(
        //language=PostgreSQL
        statement = """
            SELECT id
            FROM sak 
            WHERE person_id = :person_id
        """.trimIndent(),
        paramMap = mapOf("person_id" to personDbId),
    ).map { rad ->
        val sakId = rad.string("id")
        Sak(sakId, person)
    }.asList,
)

private fun Session.hentVedtak(personId: Long) = this.run(
    queryOf(
        //language=PostgreSQL
        statement = """
            SELECT * 
            FROM vedtak 
            WHERE person_id = :person_id 
        """.trimIndent(),
        paramMap = mapOf("person_id" to personId),
    ).map { rad ->
        val vedtakId = rad.uuid("id")
        when (VedtakTypeDTO.valueOf(rad.string("type"))) {
            VedtakTypeDTO.Ramme -> {
                Rammevedtak(
                    vedtakId = vedtakId,
                    sakId = rad.string("sak_id"),
                    behandlingId = rad.uuid("behandling_id"),
                    vedtakstidspunkt = rad.localDateTime("vedtakstidspunkt"),
                    virkningsdato = rad.localDate("virkningsdato"),
                    fakta = this.hentFakta(vedtakId = vedtakId)?.fakta ?: emptyList(),
                    rettigheter = this.hentRettigheter(vedtakId = vedtakId).map { rettighetDTO ->
                        when (rettighetDTO.rettighetstype) {
                            RettighetDTO.Rettighetstype.Ordinær -> Ordinær(rettighetDTO.utfall)
                            RettighetDTO.Rettighetstype.PermitteringFraFiskeindustrien -> TODO()
                            RettighetDTO.Rettighetstype.Permittering -> TODO()
                        }
                    },
                )
            }

            VedtakTypeDTO.Utbetaling -> {
                val utbetaling =
                    this.hentUtbetaling(vedtakId) ?: throw RuntimeException("Utbetalingsvedtak med manglende felter")
                Utbetalingsvedtak(
                    vedtakId = vedtakId,
                    sakId = rad.string("sak_id"),
                    behandlingId = rad.uuid("behandling_id"),
                    vedtakstidspunkt = rad.localDateTime("vedtakstidspunkt"),
                    virkningsdato = rad.localDate("virkningsdato"),
                    forbruk = Stønadsdager(utbetaling.forbruk),
                    utfall = utbetaling.utfall,
                    utbetalingsdager = this.hentUtbetalingsdager(vedtakId),
                )
            }

            VedtakTypeDTO.Avslag -> {
                Avslag(
                    vedtakId = vedtakId,
                    sakId = rad.string("sak_id"),
                    behandlingId = rad.uuid("behandling_id"),
                    vedtakstidspunkt = rad.localDateTime("vedtakstidspunkt"),
                    virkningsdato = rad.localDate("virkningsdato"),
                    rettigheter = this.hentRettigheter(vedtakId = vedtakId).map { rettighetDTO ->
                        when (rettighetDTO.rettighetstype) {
                            RettighetDTO.Rettighetstype.Ordinær -> Ordinær(rettighetDTO.utfall)
                            RettighetDTO.Rettighetstype.PermitteringFraFiskeindustrien -> TODO()
                            RettighetDTO.Rettighetstype.Permittering -> TODO()
                        }
                    },
                )
            }

            VedtakTypeDTO.Stans -> TODO()
        }
    }.asList,
)

private fun Session.hentUtbetaling(vedtakId: UUID) = this.run(
    queryOf(
        //language=PostgreSQL
        statement = """
            SELECT utfall, forbruk
            FROM utbetaling 
            WHERE vedtak_id = :vedtak_id
        """.trimIndent(),
        paramMap = mapOf("vedtak_id" to vedtakId),
    ).map { row ->
        UtbetalingRad(
            utfall = row.boolean("utfall"),
            forbruk = row.int("forbruk"),
        )
    }.asSingle,
)

private fun Session.hentUtbetalingsdager(vedtakId: UUID) = this.run(
    queryOf(
        //language=PostgreSQL
        statement = """
            SELECT dato, beløp
            FROM utbetalingsdag
            WHERE vedtak_id = :vedtak_id
        """.trimIndent(),
        paramMap = mapOf("vedtak_id" to vedtakId),
    ).map { rad ->
        Utbetalingsdag(dato = rad.localDate("dato"), beløp = rad.bigDecimal("beløp").beløp)
    }.asList,
)

private class UtbetalingRad(val utfall: Boolean, val forbruk: Int)

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
            WHERE vedtak.id = :vedtak_id
        """.trimIndent(),
        paramMap = mapOf("vedtak_id" to vedtakId),
    ).map { rad ->
        FaktaRad(
            dagsats = rad.bigDecimalOrNull("dagsats"),
            stønadsperiode = rad.intOrNull("stønadsperiode"),
            vanligArbeidstidPerDag = rad.bigDecimalOrNull("vanlig_arbeidstid_per_dag"),
        )
    }.asSingle,
)

private fun Session.hentRettigheter(vedtakId: UUID) = this.run(
    queryOf(
        //language=PostgreSQL
        statement = """ SELECT rettighetstype, utfall 
                |FROM rettighet
                |WHERE vedtak_id= :vedtak_id  
        """.trimMargin(),
        paramMap = mapOf("vedtak_id" to vedtakId),
    ).map { rad ->
        RettighetDTO(RettighetDTO.Rettighetstype.valueOf(rad.string("rettighetstype")), rad.boolean("utfall"))
    }.asList,
)

private fun Session.opprettPerson(ident: String) = this.run(
    queryOf(
        //language=PostgreSQL
        statement = """
            INSERT INTO person (ident) 
            VALUES (:ident) ON CONFLICT DO NOTHING RETURNING id
        """.trimIndent(),
        paramMap = mapOf("ident" to ident),
    ).map { rad -> rad.long("id") }.asSingle,
)

private fun Session.hentRapportering(rapporteringsperiodeId: UUID) = this.run(

    queryOf(
        //language=PostgreSQL
        statement = """
            SELECT  id 
            FROM    rapporteringsperiode 
            WHERE   uuid = :uuid
        """.trimIndent(),
        paramMap = mapOf("uuid" to rapporteringsperiodeId),
    ).map { rad ->
        rad.long("id")
    }.asSingle,
)

private fun Session.opprettRapportering(
    dbPersonId: Long,
    rapporteringsperiodeId: UUID,
    periode: Rapporteringsperiode,
) = this.run(

    queryOf(
        //language=PostgreSQL
        statement = """
            INSERT INTO rapporteringsperiode
                (uuid, person_id, fom, tom, endret)
            VALUES 
                (:uuid, :person_id, :fom, :tom, now())
            RETURNING id;
        """.trimIndent(),
        paramMap = mapOf(
            "uuid" to rapporteringsperiodeId,
            "person_id" to dbPersonId,
            "fom" to periode.start,
            "tom" to periode.endInclusive,
        ),
    ).map { rad ->
        rad.long("id")
    }.asSingle,
)

private fun Session.hentRapporteringsperioder(personId: Long) = this.run(
    queryOf(
        //language=PostgreSQL
        statement = """
            SELECT id, uuid, fom, tom FROM rapporteringsperiode 
            WHERE person_id = :person_id
        """.trimIndent(),
        paramMap = mapOf("person_id" to personId),
    ).map { rad ->
        val rapporteringsId = rad.uuid("uuid")
        Rapporteringsperiode(
            rapporteringsId = rapporteringsId,
            periode = Periode(rad.localDate("fom"), rad.localDate("tom")),
            dager = this.hentDager(rad.long("id")),
        )
    }.asList,
)

private fun Session.hentDager(rapporteringsperiodeId: Long) = this.run(
    queryOf(
        //language=PostgreSQL
        statement = """
            SELECT dato, syk_timer, arbeid_timer, ferie_timer
            FROM dag
            WHERE rapporteringsperiode_id = :rapporteringsperiode_id 
        """.trimIndent(),
        paramMap = mapOf(
            "rapporteringsperiode_id" to rapporteringsperiodeId,
        ),
    ).map { rad ->
        val syk = rad.doubleOrNull("syk_timer")?.let { Syk(it.timer) }
        val ferie = rad.doubleOrNull("ferie_timer")?.let { Ferie(it.timer) }
        val arbeid = rad.doubleOrNull("arbeid_timer")?.let { Arbeid(it.timer) }

        Rapporteringsdag.opprett(
            dato = rad.localDate("dato"),
            aktiviteter = listOf(syk, ferie, arbeid).mapNotNull { it },
        )
    }.asList,
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

private data class RettighetDTO(
    val rettighetstype: Rettighetstype,
    val utfall: Boolean,
) {
    enum class Rettighetstype {
        Ordinær,
        PermitteringFraFiskeindustrien,
        Permittering,
    }
}

enum class VedtakTypeDTO {
    Ramme,
    Utbetaling,
    Avslag,
    Stans,
}
