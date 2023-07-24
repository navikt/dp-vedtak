package no.nav.dagpenger.vedtak.mediator.persistens

import no.nav.dagpenger.vedtak.db.Postgres.withMigratedDb
import no.nav.dagpenger.vedtak.db.PostgresDataSourceBuilder
import no.nav.dagpenger.vedtak.modell.Person
import no.nav.dagpenger.vedtak.modell.PersonIdentifikator
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertDoesNotThrow
import kotlin.test.assertEquals

class PostgresPersonRepositoryTest {

    @Test
    fun `lagre og hent person`() {
        val ident = PersonIdentifikator("12345678901")
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
}
