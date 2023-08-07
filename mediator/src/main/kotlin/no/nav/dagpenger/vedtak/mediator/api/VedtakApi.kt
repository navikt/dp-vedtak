package no.nav.dagpenger.vedtak.mediator.api

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import no.nav.dagpenger.vedtak.mediator.api.VedtakForPersonVisitor.VedtakDto
import no.nav.dagpenger.vedtak.mediator.persistens.PersonRepository
import no.nav.dagpenger.vedtak.modell.PersonIdentifikator.Companion.tilPersonIdentfikator

fun Application.vedtakApi(personRepository: PersonRepository) {
    konfigurerApi()

    routing {
        route("vedtak") {
            post {
                val identForespørsel = call.receive<IdentForespørsel>()
                val person = personRepository.hent(identForespørsel.ident.tilPersonIdentfikator())
                val vedtakListe = mutableListOf<VedtakDto>()
                if (person != null) {
                    vedtakListe.addAll(VedtakForPersonVisitor(person).vedtakListeDto())
                }
                call.respond(HttpStatusCode.OK, vedtakListe)
            }
        }
    }
}
