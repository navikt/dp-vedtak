package no.nav.dagpenger.vedtak

import io.kotest.matchers.shouldBe
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType.Application.Json
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.server.testing.ApplicationTestBuilder
import io.ktor.server.testing.testApplication
import no.nav.dagpenger.vedtak.mediator.api.vedtakApi
import no.nav.dagpenger.vedtak.mediator.persistens.InMemoryPersonRepository
import no.nav.dagpenger.vedtak.mediator.persistens.PersonRepository
import org.junit.jupiter.api.Test

class VedtakApiTest {

    private val ident = "12345123451"

    @Test
    fun `Returner 200 OK og liste med alle vedtak for en person`() {
        withVedtakApi {
            val response = client.post("/vedtak") {
                contentType(Json)
                setBody("""{"ident": "$ident"}""")
            }
            response.status shouldBe HttpStatusCode.OK
            // "${response.contentType()}" shouldContain "application/json"
            // TODO: assert liste med vedtak
        }
    }

    @Test
    internal fun `Returner 200 OK og tom liste hvis person ikke har vedtak`() {
    }

    private fun withVedtakApi(
        personRepository: PersonRepository = InMemoryPersonRepository(),
        test: suspend ApplicationTestBuilder.() -> Unit,
    ) {
        testApplication {
            application { vedtakApi(personRepository) }
            test()
        }
    }
}
