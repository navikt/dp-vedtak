package no.nav.dagpenger.vedtak.mediator.persistens

import no.nav.dagpenger.vedtak.db.Postgres.withMigratedDb
import no.nav.dagpenger.vedtak.db.PostgresDataSourceBuilder
import no.nav.dagpenger.vedtak.modell.Person
import no.nav.dagpenger.vedtak.modell.PersonIdentifikator
import org.junit.jupiter.api.Test

class PostgresPersonRepositoryTest {

    @Test
    fun `lagre og hent person`() {
        val person = Person(ident = PersonIdentifikator("12345678901"))

        withMigratedDb {
            val postgresPersonRepository = PostgresPersonRepository(PostgresDataSourceBuilder.dataSource)
            postgresPersonRepository.lagre(person)
        }
    }
}
