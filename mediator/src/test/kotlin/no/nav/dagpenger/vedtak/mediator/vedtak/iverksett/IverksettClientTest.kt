package no.nav.dagpenger.vedtak.mediator.vedtak.iverksett

import io.kotest.matchers.shouldBe
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.runBlocking
import no.nav.dagpenger.vedtak.mediator.vedtak.iverksett.models.IverksettDagpengerdDto
import no.nav.dagpenger.vedtak.modell.vedtak.VedtakObserver
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

class IverksettClientTest {

    private val tokenProvider = { "token" }

    @Test
    fun `iverksett clienten svarer 202`() = runBlocking {
        val mockEngine = MockEngine { request ->
            request.headers[HttpHeaders.Accept] shouldBe "application/json"
            request.headers[HttpHeaders.Authorization] shouldBe "Bearer ${tokenProvider.invoke()}"
            respond(
                content = "",
                status = HttpStatusCode.Accepted,
            )
        }
        val client = IverksettClient(baseUrl = "http://localhost/", tokenProvider, mockEngine)
        client.iverksett(
            iverksettDagpengerdDtoDummy(),
        )
    }

    @Test
    fun `Om iverksett clienten svarer med 4xx og 5xx status resulterer det i exception`() = runBlocking {
        (399 until 599).forEach { statusCode ->
            val mockEngine = mockEngine(statusCode)
            val client = IverksettClient(baseUrl = "http://localhost/", tokenProvider, mockEngine)
            assertThrows<RuntimeException> {
                client.iverksett(
                    iverksettDagpengerdDtoDummy(),
                )
            }
        }
    }

    private fun iverksettDagpengerdDtoDummy(): IverksettDagpengerdDto = VedtakObserver.VedtakFattet(
        vedtakId = UUID.randomUUID(),
        behandlingId = UUID.randomUUID(),
        vedtakstidspunkt = LocalDateTime.now(),
        virkningsdato = LocalDate.now(),
        utfall = VedtakObserver.VedtakFattet.Utfall.Innvilget,

    ).tilIverksettDto("12345678911")

    private fun mockEngine(statusCode: Int) = MockEngine { request ->
        request.headers[HttpHeaders.Accept] shouldBe "application/json"
        request.headers[HttpHeaders.Authorization] shouldBe "Bearer ${tokenProvider.invoke()}"
        respond(content = "", HttpStatusCode.fromValue(statusCode))
    }
}
