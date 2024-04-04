package no.nav.dagpenger.behandling.mediator.api

import `in`.specmatic.test.SpecmaticJUnitSupport
import io.ktor.server.cio.CIO
import io.ktor.server.engine.ApplicationEngine
import io.ktor.server.engine.embeddedServer
import io.mockk.mockk
import no.nav.dagpenger.behandling.db.InMemoryPersonRepository
import no.nav.dagpenger.behandling.db.Postgres.withMigratedDb
import no.nav.dagpenger.behandling.modell.Ident.Companion.tilPersonIdentfikator
import no.nav.dagpenger.behandling.modell.Person
import no.nav.dagpenger.behandling.modell.hendelser.OpplysningSvar
import no.nav.dagpenger.behandling.modell.hendelser.OpplysningSvarHendelse
import no.nav.dagpenger.behandling.modell.hendelser.SøknadInnsendtHendelse
import no.nav.dagpenger.opplysning.Saksbehandlerkilde
import no.nav.dagpenger.opplysning.UUIDv7
import no.nav.dagpenger.regel.Søknadstidspunkt
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import java.time.LocalDate

class ContractTests : SpecmaticJUnitSupport() {
    companion object {
        private lateinit var server: ApplicationEngine

        private val ident = "12345123451"
        private val person =
            Person(ident.tilPersonIdentfikator()).also {
                it.håndter(
                    SøknadInnsendtHendelse(
                        meldingsreferanseId = UUIDv7.ny(),
                        ident = ident,
                        søknadId = UUIDv7.ny(),
                        gjelderDato = LocalDate.now(),
                        fagsakId = 1,
                    ),
                )
                it.håndter(
                    OpplysningSvarHendelse(
                        meldingsreferanseId = UUIDv7.ny(),
                        ident = ident,
                        behandlingId = it.behandlinger().first().behandlingId,
                        opplysninger =
                            listOf(
                                OpplysningSvar(
                                    opplysningstype = Søknadstidspunkt.søknadsdato,
                                    verdi = LocalDate.now(),
                                    tilstand = OpplysningSvar.Tilstand.Faktum,
                                    kilde = Saksbehandlerkilde("Z123456"),
                                ),
                            ),
                    ),
                )
            }
        private val personRepository =
            InMemoryPersonRepository().also {
                it.lagre(person)
            }

        @JvmStatic
        @BeforeAll
        fun setUp() {
            System.setProperty("host", "localhost")
            System.setProperty("port", "8081")

            // System.setProperty("SPECMATIC_GENERATIVE_TESTS", "true")
            withMigratedDb {
                server =
                    embeddedServer(CIO, port = 8081) {
                        behandlingApi(
                            personRepository = personRepository,
                            personMediator = mockk(relaxed = true),
                            auditlogg = mockk(relaxed = true),
                        )
                    }.start()
            }
        }

        @JvmStatic
        @AfterAll
        fun tearDown() {
            server.stop(1000, 1000)
        }
    }
}
