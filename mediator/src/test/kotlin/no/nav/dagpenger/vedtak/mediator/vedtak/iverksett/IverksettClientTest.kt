package no.nav.dagpenger.vedtak.mediator.vedtak.iverksett

import io.kotest.matchers.shouldBe
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import kotlinx.coroutines.runBlocking
import no.nav.dagpenger.vedtak.mediator.vedtak.iverksett.models.BehandlingsdetaljerDto
import no.nav.dagpenger.vedtak.mediator.vedtak.iverksett.models.FagsakdetaljerDto
import no.nav.dagpenger.vedtak.mediator.vedtak.iverksett.models.IverksettDagpengerDto
import no.nav.dagpenger.vedtak.mediator.vedtak.iverksett.models.SøkerDto
import no.nav.dagpenger.vedtak.mediator.vedtak.iverksett.models.VedtaksdetaljerDagpengerDto
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDateTime
import java.util.UUID
import kotlin.random.Random

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

    private fun mockEngine(statusCode: Int) = MockEngine { request ->
        request.headers[HttpHeaders.Accept] shouldBe "application/json"
        request.headers[HttpHeaders.Authorization] shouldBe "Bearer ${tokenProvider.invoke()}"
        respond(content = "", HttpStatusCode.fromValue(statusCode))
    }

    private fun iverksettDagpengerdDtoDummy() = IverksettDagpengerDto(
        fagsak = FagsakdetaljerDto(
            fagsakId = UUID.randomUUID(),
            eksternId = Random.nextLong(),
            stønadstype = FagsakdetaljerDto.Stønadstype.DAGPENGER,
        ),
        behandling = BehandlingsdetaljerDto(
            behandlingId = UUID.randomUUID(),
            behandlingType = BehandlingsdetaljerDto.BehandlingType.FØRSTEGANGSBEHANDLING,
            eksternId = Random.nextLong(),
            behandlingårsak = BehandlingsdetaljerDto.Behandlingårsak.SØKNAD,
            vilkårsvurderinger = emptyList(),
        ),
        søkerDto = SøkerDto(
            personIdent = "123456789",
            barn = emptyList(),
            tilhørendeEnhet = "",
        ),
        vedtak = VedtaksdetaljerDagpengerDto(
            vedtakstidspunkt = LocalDateTime.now(),
            resultat = VedtaksdetaljerDagpengerDto.Resultat.INNVILGET,
            opphørårsak = null,
            avslagårsak = null,
            saksbehandlerId = "",
            beslutterId = "",
            utbetalinger = listOf(),
            vedtaksperioder = listOf(),
            tilbakekreving = null,
            brevmottakere = listOf(),
        ),
        forrigeVedtak = null,

    )
}
