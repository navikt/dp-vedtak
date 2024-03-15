package no.nav.dagpenger.behandling.mediator.repository

import io.kotest.matchers.collections.shouldContain
import no.nav.dagpenger.behandling.db.Postgres.withMigratedDb
import no.nav.dagpenger.behandling.modell.Behandling
import no.nav.dagpenger.behandling.modell.Ident
import no.nav.dagpenger.behandling.modell.Person
import no.nav.dagpenger.behandling.modell.UUIDv7
import no.nav.dagpenger.behandling.modell.hendelser.SøknadInnsendtHendelse
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import java.time.LocalDate

class PersonRepositoryPostgresTest {
    private val fnr = "12345678901"
    private val søknadId = UUIDv7.ny()
    private val personRepositoryPostgres get() = PersonRepositoryPostgres(BehandlingRepositoryPostgres(OpplysningerRepositoryPostgres()))
    private val søknadInnsendtHendelse =
        SøknadInnsendtHendelse(
            søknadId = søknadId,
            ident = fnr,
            meldingsreferanseId = søknadId,
            gjelderDato = LocalDate.now(),
        )

    @Test
    fun `hent returnerer person når personen finnes i databasen`() =
        withMigratedDb {
            val ident = Ident(fnr)
            val expectedPerson =
                Person(ident, emptyList()).also {
                    personRepositoryPostgres.lagre(it)
                }

            val actualPerson = personRepositoryPostgres.hent(ident)

            assertEquals(expectedPerson.ident, actualPerson?.ident)
        }

    @Test
    fun `hent returnerer null når personen ikke finnes i databasen`() =
        withMigratedDb {
            val ident = Ident(fnr)

            val actualPerson = personRepositoryPostgres.hent(ident)

            assertNull(actualPerson)
        }

    @Test
    fun `lagre setter inn person og deres behandlinger i databasen`() =
        withMigratedDb {
            val ident = Ident(fnr)
            val behandling = Behandling(søknadInnsendtHendelse, emptyList())
            val person = Person(ident, listOf(behandling))

            personRepositoryPostgres.lagre(person)

            val fraDb = personRepositoryPostgres.hent(ident)
            fraDb?.let {
                assertEquals(person.ident, it.ident)
                person.behandlinger() shouldContain behandling
            }
        }

    @Test
    fun `lagre setter ikke inn person i databasen når personen allerede finnes`() =
        withMigratedDb {
            val ident = Ident(fnr)
            val behandling = Behandling(søknadInnsendtHendelse, emptyList())
            val person = Person(ident, listOf(behandling))

            personRepositoryPostgres.lagre(person)
            personRepositoryPostgres.lagre(person)
        }
}
