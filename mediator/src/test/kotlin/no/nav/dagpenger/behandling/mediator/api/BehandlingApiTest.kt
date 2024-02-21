package no.nav.dagpenger.behandling.mediator.api

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldNotBeEmpty
import io.ktor.client.call.body
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.ApplicationTestBuilder
import no.nav.dagpenger.behandling.api.models.BehandlingDTO
import no.nav.dagpenger.behandling.db.InMemoryPersonRepository
import no.nav.dagpenger.behandling.mediator.PersonRepository
import no.nav.dagpenger.behandling.mediator.api.TestApplication.autentisert
import no.nav.dagpenger.behandling.mediator.api.TestApplication.testAzureAdToken
import no.nav.dagpenger.behandling.modell.Person
import no.nav.dagpenger.behandling.modell.PersonIdentifikator.Companion.tilPersonIdentfikator
import no.nav.dagpenger.behandling.modell.hendelser.OpplysningSvar
import no.nav.dagpenger.behandling.modell.hendelser.OpplysningSvarHendelse
import no.nav.dagpenger.behandling.modell.hendelser.SøknadInnsendtHendelse
import no.nav.dagpenger.opplysning.UUIDv7
import no.nav.dagpenger.regel.Virkningsdato
import no.nav.security.mock.oauth2.http.objectMapper
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import java.time.LocalDate

internal class BehandlingApiTest {
    private val ident = "12345123451"
    val person =
        Person(ident.tilPersonIdentfikator()).also {
            it.håndter(
                SøknadInnsendtHendelse(
                    meldingsreferanseId = UUIDv7.ny(),
                    ident = ident,
                    søknadId = UUIDv7.ny(),
                    gjelderDato = LocalDate.now(),
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
                                opplysningstype = Virkningsdato.søknadsdato,
                                verdi = LocalDate.now(),
                                tilstand = OpplysningSvar.Tilstand.Faktum,
                            ),
                        ),
                ),
            )
        }
    private val personRepository =
        InMemoryPersonRepository().also {
            it.lagre(person)
        }

    @AfterEach
    fun tearDown() {
        personRepository.reset()
    }

    @Test
    fun `ikke autentiserte kall returnerer 401`() {
        medSikretBehandlingApi {
            val response =
                client.post("/behandling") {
                    setBody("""{"ident":"$ident"}""")
                }
            response.status shouldBe HttpStatusCode.Unauthorized
        }
    }

    @Test
    fun `kall uten saksbehandlingsADgruppe i claims returnerer 401`() {
        medSikretBehandlingApi {
            val tokenUtenSaksbehandlerGruppe = testAzureAdToken(ADGrupper = emptyList())

            val response =
                autentisert(
                    token = tokenUtenSaksbehandlerGruppe,
                    endepunkt = "/behandling",
                    body = """{"ident":"$ident"}""",
                )
            response.status shouldBe HttpStatusCode.Unauthorized
        }
    }

    @Test
    fun `hent behandlinger gitt person`() {
        medSikretBehandlingApi {
            val response = autentisert("/behandling", body = """{"ident":"$ident"}""")
            response.status shouldBe HttpStatusCode.OK
            response.bodyAsText().shouldNotBeEmpty()
        }
    }

    @Test
    fun `gir 404 hvis person ikke eksisterer`() {
        medSikretBehandlingApi {
            val response = autentisert("/behandling", body = """{"ident":"09876543311"}""")
            response.status shouldBe HttpStatusCode.NotFound
        }
    }

    @Test
    fun `hent behandling gitt behandlingId`() {
        medSikretBehandlingApi {
            val behandlingId = person.behandlinger().first().behandlingId
            val response = autentisert(httpMethod = HttpMethod.Get, endepunkt = "/behandling/$behandlingId")
            response.status shouldBe HttpStatusCode.OK
            response.bodyAsText().shouldNotBeEmpty()
            val behandling = shouldNotThrowAny { objectMapper.readValue(response.bodyAsText(), BehandlingDTO::class.java) }
            behandling.behandlingId shouldBe behandlingId
            behandling.opplysning.size shouldBe 8
        }
    }

    private fun medSikretBehandlingApi(
        personRepository: PersonRepository = this.personRepository,
        test: suspend ApplicationTestBuilder.() -> Unit,
    ) {
        TestApplication.withMockAuthServerAndTestApplication(
            moduleFunction = {
                behandlingApi(personRepository)
            },
            test,
        )
    }

    private companion object {
        private val objectMapper =
            jacksonObjectMapper().registerModule(JavaTimeModule()).disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    }
}
