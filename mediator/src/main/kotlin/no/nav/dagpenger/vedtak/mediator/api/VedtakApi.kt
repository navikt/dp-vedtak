package no.nav.dagpenger.vedtak.mediator.api

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import no.nav.dagpenger.vedtak.mediator.persistens.PersonRepository
import no.nav.dagpenger.vedtak.modell.PersonIdentifikator.Companion.tilPersonIdentfikator

fun Application.vedtakApi(personRepository: PersonRepository) {
    konfigurerApi()

    routing {
        route("vedtak") {
            post {
                val identDto = call.receive<IdentDto>()
                val person = personRepository.hent(identDto.ident.tilPersonIdentfikator())
                // TODO: Lag visitor som henter ut alle vedtak fra person og deretter lager en vedtakListeDto. returner dette objektet
                call.respond(HttpStatusCode.OK)
            }
        }
    }
}
