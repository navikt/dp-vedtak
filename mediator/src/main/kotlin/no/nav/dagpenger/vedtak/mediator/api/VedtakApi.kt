package no.nav.dagpenger.vedtak.mediator.api

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.plugins.swagger.swaggerUI
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import no.nav.dagpenger.vedtak.api.models.IdentForesporselDTO
import no.nav.dagpenger.vedtak.api.models.VedtakDTO
import no.nav.dagpenger.vedtak.mediator.persistens.PersonRepository
import no.nav.dagpenger.vedtak.modell.PersonIdentifikator.Companion.tilPersonIdentfikator

fun Application.vedtakApi(personRepository: PersonRepository) {
    konfigurerApi()

    routing {
        swaggerUI(path = "openapi", swaggerFile = "vedtak-api.yaml")

        authenticate("azureAd") {
            route("vedtak") {
                post {
                    val identForespørsel = call.receive<IdentForesporselDTO>()
                    val person = personRepository.hent(identForespørsel.ident.tilPersonIdentfikator())
                    call.respond(HttpStatusCode.OK, VedtakDTO(rammer = emptyList(), utbetalinger = emptyList()))
                }
            }
        }
    }
}
