package no.nav.dagpenger.behandling.mediator.api

import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.auth.authenticate
import io.ktor.server.plugins.swagger.swaggerUI
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.route
import io.ktor.server.routing.routing
import no.nav.dagpenger.behandling.api.models.BehandlingDTO
import no.nav.dagpenger.behandling.api.models.IdentForesporselDTO
import no.nav.dagpenger.behandling.api.models.OpplysningDTO
import no.nav.dagpenger.behandling.mediator.PersonRepository
import no.nav.dagpenger.behandling.modell.Behandling
import no.nav.dagpenger.behandling.modell.PersonIdentifikator.Companion.tilPersonIdentfikator
import no.nav.dagpenger.opplysning.Faktum
import no.nav.dagpenger.opplysning.Hypotese
import org.apache.kafka.common.errors.ResourceNotFoundException
import java.time.ZoneOffset

fun Application.behandlingApi(personRepository: PersonRepository) {
    konfigurerApi()

    routing {
        swaggerUI(path = "openapi", swaggerFile = "behandling-api.yaml")

        authenticate("azureAd") {
            route("behandling") {
                post {
                    val identForespørsel = call.receive<IdentForesporselDTO>()
                    val person =
                        personRepository.hent(
                            identForespørsel.ident.tilPersonIdentfikator(),
                        ) ?: throw ResourceNotFoundException("Person ikke funnet")
                    call.respond(HttpStatusCode.OK, person.behandlinger().map { it.tilBehandlingDTO() })
                }
            }
        }
    }
}

private fun Behandling.tilBehandlingDTO() =
    BehandlingDTO(
        behandlingId = this.behandlingId,
        opplysning =
            this.opplysninger().map { opplysning ->
                OpplysningDTO(
                    id = opplysning.id,
                    opplysningstype = opplysning.opplysningstype.id,
                    verdi = opplysning.verdi.toString(),
                    status =
                        when (opplysning) {
                            is Faktum -> OpplysningDTO.Status.Faktum
                            is Hypotese -> OpplysningDTO.Status.Hypotese
                        },
                    gyldigFraOgMed = opplysning.gyldighetsperiode.fom.atOffset(ZoneOffset.of("+01:00")),
                    gyldigTilOgMed = opplysning.gyldighetsperiode.tom.atOffset(ZoneOffset.of("+01:00")),
                    datatype = opplysning.opplysningstype.datatype.klasse.simpleName,
                    kilde = null,
                    utledetAv = null,
                )
            },
    )
