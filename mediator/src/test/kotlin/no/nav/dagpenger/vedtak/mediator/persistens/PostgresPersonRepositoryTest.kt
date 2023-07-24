package no.nav.dagpenger.vedtak.mediator.persistens

import no.nav.dagpenger.vedtak.assertDeepEquals
import no.nav.dagpenger.vedtak.db.Postgres.withMigratedDb
import no.nav.dagpenger.vedtak.db.PostgresDataSourceBuilder
import no.nav.dagpenger.vedtak.modell.Dagpengerettighet
import no.nav.dagpenger.vedtak.modell.Person
import no.nav.dagpenger.vedtak.modell.PersonIdentifikator
import no.nav.dagpenger.vedtak.modell.entitet.Beløp.Companion.beløp
import no.nav.dagpenger.vedtak.modell.entitet.Dagpengeperiode
import no.nav.dagpenger.vedtak.modell.entitet.Timer.Companion.timer
import no.nav.dagpenger.vedtak.modell.hendelser.DagpengerInnvilgetHendelse
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import java.time.LocalDate
import java.util.UUID
import kotlin.test.assertEquals

class PostgresPersonRepositoryTest {

    val ident = PersonIdentifikator("12345678901")

    @Test
    fun `lagre og hent person`() {
        val person = Person(ident = ident)

        withMigratedDb {
            val postgresPersonRepository = PostgresPersonRepository(PostgresDataSourceBuilder.dataSource)
            postgresPersonRepository.lagre(person)
            assertDoesNotThrow {
                postgresPersonRepository.lagre(person)
            }
            val hentetPerson = postgresPersonRepository.hent(ident)
            assertEquals(person.ident(), hentetPerson?.ident())
        }
    }

    @Test
    fun `lagring og henter en komplett person`() {
        val person = Person(ident = ident)
        person.håndter(
            DagpengerInnvilgetHendelse(
                ident = ident.identifikator(),
                behandlingId = UUID.randomUUID(),
                virkningsdato = LocalDate.now(),
                dagpengerettighet = Dagpengerettighet.Ordinær,
                dagsats = 800.beløp,
                grunnlag = 3333333.toBigDecimal(),
                stønadsdager = Dagpengeperiode(52).tilStønadsdager(),
                vanligArbeidstidPerDag = 8.timer,
                egenandel = 800.beløp * 3,
            ),
        )

        withMigratedDb {
            val personRepository = PostgresPersonRepository(PostgresDataSourceBuilder.dataSource)
            personRepository.lagre(person)

            val rehydrertPerson = personRepository.hent(ident)

            assertDeepEquals(
                person,
                rehydrertPerson,
            )
        }
    }
}
