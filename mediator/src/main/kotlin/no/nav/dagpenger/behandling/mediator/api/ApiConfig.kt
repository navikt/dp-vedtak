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
import io.ktor.server.response.respond
import no.nav.dagpenger.behandling.mediator.api.auth.AuthFactory.azureAd
import org.apache.kafka.common.errors.ResourceNotFoundException
import org.slf4j.event.Level

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
            call.respond(HttpStatusCode.BadRequest, cause.message ?: "Feil med forespørsel")
        }
        exception<ResourceNotFoundException> { call: ApplicationCall, cause: ResourceNotFoundException ->
            call.respond(HttpStatusCode.NotFound, cause.message ?: "Fant ikke ressurs")
        }
    }
}
