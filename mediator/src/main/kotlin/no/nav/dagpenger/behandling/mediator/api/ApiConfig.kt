package no.nav.dagpenger.behandling.mediator.api

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.jackson.jackson
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.AuthenticationConfig
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.plugins.callloging.CallLogging
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.statuspages.StatusPages
import io.ktor.server.request.path
import io.ktor.server.request.uri
import io.ktor.server.response.respond
import mu.KotlinLogging
import no.nav.dagpenger.behandling.api.models.HttpProblemDTO
import no.nav.dagpenger.behandling.mediator.api.auth.AuthFactory.azureAd
import org.apache.kafka.common.errors.ResourceNotFoundException
import org.slf4j.event.Level
import java.net.URI

private val logger = KotlinLogging.logger {}

internal fun Application.konfigurerApi(
    auth: AuthenticationConfig.() -> Unit = {
        jwt("azureAd") { azureAd() }
    },
) {
    install(ContentNegotiation) {
        jackson {
            registerModule(JavaTimeModule())
            disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
        }
    }
    install(CallLogging) {
        disableDefaultColors()
        filter {
            it.request.path() !in setOf("/metrics", "/isalive", "/isready")
        }
        level = Level.INFO
    }
    install(Authentication) {
        auth()
    }
    install(StatusPages) {
        exception<IllegalArgumentException> { call: ApplicationCall, cause: IllegalArgumentException ->
            val problem =
                HttpProblemDTO(
                    type = URI.create(call.request.uri),
                    title = "Ugyldig forespørsel",
                    status = HttpStatusCode.BadRequest.value,
                    detail = cause.message ?: "Ugyldig forespørsel",
                )
            logger.error(cause) { "Noe gikk galt på ${call.request.uri}" }
            call.respond(HttpStatusCode.BadRequest, problem)
        }
        exception<ResourceNotFoundException> { call: ApplicationCall, cause: ResourceNotFoundException ->
            val problem =
                HttpProblemDTO(
                    type = URI.create(call.request.uri),
                    title = "Fant ikke ressurs",
                    status = HttpStatusCode.NotFound.value,
                    detail = cause.message ?: "Fant ikke ressurs",
                )
            logger.error(cause) { "Noe gikk galt på ${call.request.uri}" }
            call.respond(HttpStatusCode.NotFound, problem)
        }
        exception<Throwable> { call: ApplicationCall, cause: Throwable ->
            val problem =
                HttpProblemDTO(
                    type = URI.create(call.request.uri),
                    title = "Noe gikk galt",
                    status = HttpStatusCode.InternalServerError.value,
                    detail = cause.message ?: "Noe gikk galt",
                )
            logger.error(cause) { "Noe gikk galt på ${call.request.uri}" }
            call.respond(HttpStatusCode.InternalServerError, problem)
        }
    }
}
