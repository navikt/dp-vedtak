package no.nav.dagpenger.vedtak.mediator.api

import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType.Application.Json
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.testing.ApplicationTestBuilder
import no.nav.dagpenger.vedtak.db.InMemoryPersonRepository
import no.nav.dagpenger.vedtak.mediator.api.TestApplication.autentisert
import no.nav.dagpenger.vedtak.mediator.api.TestApplication.testAzureAdToken
import no.nav.dagpenger.vedtak.mediator.persistens.PersonRepository
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test

class VedtakApiTest {
    private val ident = "12345123451"
    private val personRepository = InMemoryPersonRepository()

    @AfterEach
    fun tearDown() {
        personRepository.reset()
    }

    @Test
    fun `ikke autentiserte kall returnerer 401`() {
        medSikretVedtakApi {
            val response =
                client.post("/vedtak") {
                    contentType(Json)
                    setBody("""{"ident": "$ident"}""")
                }
            response.status shouldBe HttpStatusCode.Unauthorized
        }
    }

    @Test
    fun `kall uten saksbehandlingsADgruppe i claims returnerer 401`() {
        medSikretVedtakApi {
            val tokenUtenSaksbehandlerGruppe = testAzureAdToken(ADGrupper = emptyList())

            val response =
                autentisert(
                    token = tokenUtenSaksbehandlerGruppe,
                    endepunkt = "/vedtak",
                    body = """{"ident": "$ident"}""",
                )
            response.status shouldBe HttpStatusCode.Unauthorized
        }
    }

    @Test
    internal fun `200 OK og tom liste av ramme- og utbetalingsvedtak hvis person ikke eksisterer i db`() {
        medSikretVedtakApi {
            val response = autentisert(endepunkt = "/vedtak", body = """{"ident": "$ident"}""")

            print(response.bodyAsText())

            response.status shouldBe HttpStatusCode.OK
            response.contentType().toString() shouldContain "application/json"
            response.bodyAsText() shouldContain "\"rammer\":[]"
            response.bodyAsText() shouldContain "\"utbetalinger\":[]"
        }
    }

    private fun medSikretVedtakApi(
        personRepository: PersonRepository = this.personRepository,
        test: suspend ApplicationTestBuilder.() -> Unit,
    ) {
        TestApplication.withMockAuthServerAndTestApplication(
            moduleFunction = {
                vedtakApi(personRepository)
            },
            test,
        )
    }
}
