package no.nav.dagpenger.vedtak.mediator.persistens

import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe
import no.nav.dagpenger.vedtak.assertDeepEquals
import no.nav.dagpenger.vedtak.db.Postgres.withMigratedDb
import no.nav.dagpenger.vedtak.db.PostgresDataSourceBuilder
import no.nav.dagpenger.vedtak.modell.Dagpengerettighet
import no.nav.dagpenger.vedtak.modell.Person
import no.nav.dagpenger.vedtak.modell.PersonIdentifikator
import no.nav.dagpenger.vedtak.modell.entitet.Beløp.Companion.beløp
import no.nav.dagpenger.vedtak.modell.entitet.Dagpengeperiode
import no.nav.dagpenger.vedtak.modell.entitet.Periode
import no.nav.dagpenger.vedtak.modell.entitet.Timer.Companion.timer
import no.nav.dagpenger.vedtak.modell.hendelser.DagpengerInnvilgetHendelse
import no.nav.dagpenger.vedtak.modell.hendelser.Rapporteringsdag
import no.nav.dagpenger.vedtak.modell.hendelser.Rapporteringshendelse
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.util.Random
import java.util.UUID
import kotlin.time.Duration.Companion.hours

class PostgresPersonRepositoryTest {

    val ident = PersonIdentifikator("12345678901")

    @Test
    fun `lagre og hent person`() {
        val person = Person(ident = ident)

        withMigratedDb {
            val postgresPersonRepository = PostgresPersonRepository(PostgresDataSourceBuilder.dataSource)
            postgresPersonRepository.lagre(person)
            shouldNotThrowAny {
                postgresPersonRepository.lagre(person)
            }
            val hentetPerson = postgresPersonRepository.hent(ident).shouldNotBeNull()
            person.ident() shouldBe hentetPerson.ident()
        }
    }

    @Test
    fun `lagring og henter en komplett person`() {
        val person = Person(ident = ident)
        val idag = LocalDate.now()
        person.håndter(
            DagpengerInnvilgetHendelse(
                ident = ident.identifikator(),
                behandlingId = UUID.randomUUID(),
                virkningsdato = idag,
                dagpengerettighet = Dagpengerettighet.Ordinær,
                dagsats = 800.beløp,
                stønadsdager = Dagpengeperiode(52).tilStønadsdager(),
                vanligArbeidstidPerDag = 8.timer,
                egenandel = 800.beløp * 3,
            ),
        )

        val periode = Periode(
            idag,
            idag.plusDays(14),
        )

        val rapporteringdager = periode.map {
            Rapporteringsdag(
                dato = it,
                aktiviteter = listOf(
                    Rapporteringsdag.Aktivitet(hentTilfeldigAktivitet(), 8.hours),
                ),
            )
        }

        person.håndter(
            Rapporteringshendelse(
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
        }
    }

    private fun hentTilfeldigAktivitet(): Rapporteringsdag.Aktivitet.Type {
        val antallAktiviteter = Rapporteringsdag.Aktivitet.Type.values().size
        return Rapporteringsdag.Aktivitet.Type.values()[
            kotlin.random.Random.nextInt(
                0,
                antallAktiviteter - 1,
            ),
        ]
    }
}
