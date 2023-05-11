package no.nav.dagpenger.vedtak.iverksetting.mediator.behovløsere

import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import io.ktor.client.HttpClient
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.URLBuilder
import io.ktor.http.appendEncodedPathSegments
import io.ktor.serialization.jackson.jackson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import no.nav.dagpenger.vedtak.iverksetting.mediator.behovløsere.models.IverksettDagpengerdDto
import no.nav.dagpenger.vedtak.mediator.Configuration

internal class IverksettClient(
    private val baseUrl: String = Configuration.iverksettApiUrl,
    private val tokenProvider: () -> String,
    engine: HttpClientEngine = CIO.create {},
) {

    private companion object {
        val sikkerLogg = KotlinLogging.logger("tjenestekall.IverksettClient")
    }

    private val httpClient = HttpClient(engine) {
        expectSuccess = true

        defaultRequest {
            header(HttpHeaders.Authorization, "Bearer ${tokenProvider.invoke()}")
        }
        install(ContentNegotiation) {
            jackson {
                registerModule(JavaTimeModule())
                disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            }
        }
        install(Logging) {
            level = LogLevel.ALL
            logger = object : Logger {
                override fun log(message: String) {
                    sikkerLogg.info(message)
                }
            }
            sanitizeHeader { header -> header == HttpHeaders.Authorization }
        }
    }

    suspend fun iverksett(iverksettDagpengerDto: IverksettDagpengerdDto) {
        val url = URLBuilder(baseUrl).appendEncodedPathSegments("api", "iverksetting").build()
        withContext(Dispatchers.IO) {
            httpClient.post(url) {
                header(HttpHeaders.ContentType, ContentType.Application.Json)
                setBody(iverksettDagpengerDto)
            }
        }
    }
}
