package no.nav.dagpenger.vedtak.mediator.persistens

import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import kotliquery.queryOf
import kotliquery.sessionOf
import kotliquery.using
import no.nav.dagpenger.vedtak.assertDeepEquals
import no.nav.dagpenger.vedtak.db.Postgres.withMigratedDb
import no.nav.dagpenger.vedtak.db.PostgresDataSourceBuilder
import no.nav.dagpenger.vedtak.modell.Person
import no.nav.dagpenger.vedtak.modell.PersonIdentifikator
import no.nav.dagpenger.vedtak.modell.entitet.Beløp.Companion.beløp
import no.nav.dagpenger.vedtak.modell.entitet.Dagpengeperiode
import no.nav.dagpenger.vedtak.modell.entitet.Periode
import no.nav.dagpenger.vedtak.modell.entitet.Timer.Companion.timer
import no.nav.dagpenger.vedtak.modell.hendelser.DagpengerAvslåttHendelse
import no.nav.dagpenger.vedtak.modell.hendelser.DagpengerInnvilgetHendelse
import no.nav.dagpenger.vedtak.modell.hendelser.Rapporteringsdag
import no.nav.dagpenger.vedtak.modell.hendelser.Rapporteringshendelse
import no.nav.dagpenger.vedtak.modell.vedtak.rettighet.Ordinær
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.UUID

class PostgresPersonRepositoryTest {

    val ident = PersonIdentifikator("12345678901")

    @Test
    fun `lagrer og henter person`() {
        val person = Person(ident = ident)

        withMigratedDb {
            val postgresPersonRepository = PostgresPersonRepository(PostgresDataSourceBuilder.dataSource)
            postgresPersonRepository.lagre(person)
            shouldNotThrowAny {
                postgresPersonRepository.lagre(person)
            }
            val hentetPerson = postgresPersonRepository.hent(ident).shouldNotBeNull()
            person.ident() shouldBe hentetPerson.ident()

            assertAntallRader("person", 1)
        }
    }

    @Test
    fun `lagrer og henter komplett person med innvilgelse`() {
        val person = Person(ident = ident)
        val idag = LocalDate.now()
        person.håndter(
            DagpengerInnvilgetHendelse(
                meldingsreferanseId = UUID.randomUUID(),
                sakId = "SAK_NUMMER_1",
                ident = ident.identifikator(),
                behandlingId = UUID.randomUUID(),
                vedtakstidspunkt = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS),
                virkningsdato = idag,
                hovedrettighet = Ordinær(true),
                dagsats = 800.beløp,
                stønadsdager = Dagpengeperiode(52).tilStønadsdager(),
                vanligArbeidstidPerDag = 8.timer,
            ),
        )

        val periode = Periode(
            idag,
            idag.plusDays(13),
        )

        val rapporteringdager = periode.map {
            Rapporteringsdag(
                dato = it,
                aktiviteter = emptyList(),
            )
        }

        person.håndter(
            Rapporteringshendelse(
                meldingsreferanseId = UUID.randomUUID(),
                ident = ident.identifikator(),
                rapporteringsId = UUID.randomUUID(),
                rapporteringsdager = rapporteringdager,
                fom = periode.start,
                tom = periode.endInclusive,

            ),
        )

        withMigratedDb {
            val personRepository = PostgresPersonRepository(PostgresDataSourceBuilder.dataSource)
            personRepository.lagre(person)

            val rehydrertPerson = personRepository.hent(ident).shouldNotBeNull()

            assertDeepEquals(
                person,
                rehydrertPerson,
            )

            assertAntallRader("person", 1)
            assertAntallRader("vedtak", 2)
            assertAntallRader("dagsats", 1)
            assertAntallRader("stønadsperiode", 1)
            assertAntallRader("vanlig_arbeidstid", 1)
            assertAntallRader("rettighet", 1)
            assertAntallRader("utbetaling", 1)
            assertAntallRader("utbetalingsdag", 10)
            assertAntallRader("rapporteringsperiode", 1)
            assertAntallRader("dag", 14)
        }
    }

    @Test
    fun `lagrer og henter komplett person med avslag`() {
        val person = Person(ident = ident)
        val idag = LocalDate.now()
        person.håndter(
            DagpengerAvslåttHendelse(
                meldingsreferanseId = UUID.randomUUID(),
                sakId = "SAK_NUMMER_1",
                ident = ident.identifikator(),
                behandlingId = UUID.randomUUID(),
                vedtakstidspunkt = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS),
                virkningsdato = idag,
                hovedrettighet = Ordinær(false),
            ),
        )

        withMigratedDb {
            val personRepository = PostgresPersonRepository(PostgresDataSourceBuilder.dataSource)
            personRepository.lagre(person)

            val rehydrertPerson = personRepository.hent(ident).shouldNotBeNull()

            assertDeepEquals(
                person,
                rehydrertPerson,
            )
        }
    }

    private fun assertAntallRader(tabell: String, antallRader: Int) {
        val faktiskeRader = using(sessionOf(PostgresDataSourceBuilder.dataSource)) { session ->
            session.run(
                queryOf("select count(1) from $tabell").map { row ->
                    row.int(1)
                }.asSingle,
            )
        }
        Assertions.assertEquals(antallRader, faktiskeRader, "Feil antall rader for tabell: $tabell")
    }
}
