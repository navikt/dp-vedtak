package no.nav.dagpenger.behandling.mediator

import com.github.navikt.tbd_libs.rapids_and_rivers.KafkaRapid
import com.github.navikt.tbd_libs.rapids_and_rivers_api.RapidsConnection
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.ApplicationCall
import io.ktor.server.plugins.statuspages.StatusPagesConfig
import io.ktor.server.request.uri
import io.ktor.server.response.respond
import mu.KotlinLogging
import no.nav.dagpenger.behandling.api.models.HttpProblemDTO
import no.nav.dagpenger.behandling.db.PostgresDataSourceBuilder.clean
import no.nav.dagpenger.behandling.db.PostgresDataSourceBuilder.runMigration
import no.nav.dagpenger.behandling.konfigurasjon.Configuration.config
import no.nav.dagpenger.behandling.konfigurasjon.støtterInnvilgelse
import no.nav.dagpenger.behandling.mediator.api.ApiMessageContext
import no.nav.dagpenger.behandling.mediator.api.behandlingApi
import no.nav.dagpenger.behandling.mediator.audit.ApiAuditlogg
import no.nav.dagpenger.behandling.mediator.jobber.AvbrytInnvilgelse
import no.nav.dagpenger.behandling.mediator.melding.PostgresHendelseRepository
import no.nav.dagpenger.behandling.mediator.mottak.ArenaOppgaveMottak
import no.nav.dagpenger.behandling.mediator.mottak.SakRepository
import no.nav.dagpenger.behandling.mediator.repository.AvklaringKafkaObservatør
import no.nav.dagpenger.behandling.mediator.repository.AvklaringRepositoryPostgres
import no.nav.dagpenger.behandling.mediator.repository.BehandlingRepositoryPostgres
import no.nav.dagpenger.behandling.mediator.repository.OpplysningerRepositoryPostgres
import no.nav.dagpenger.behandling.mediator.repository.PersonRepositoryPostgres
import no.nav.dagpenger.behandling.objectMapper
import no.nav.dagpenger.opplysning.Opplysningstype
import no.nav.dagpenger.regel.RegelverkDagpenger
import no.nav.helse.rapids_rivers.RapidApplication
import org.apache.kafka.common.errors.ResourceNotFoundException
import java.net.URI

internal class ApplicationBuilder(
    config: Map<String, String>,
) : RapidsConnection.StatusListener {
    companion object {
        private val logger = KotlinLogging.logger { }
    }

    // TODO: Last alle regler ved startup. Dette må inn i ett register.
    private val opplysningstyper: Set<Opplysningstype<*>> = RegelverkDagpenger.produserer

    private val rapidsConnection: RapidsConnection =
        RapidApplication.create(env = config, objectMapper = objectMapper, builder = {
            withStatusPagesConfig(statusPages())
        }) { engine, rapidsConnection: KafkaRapid ->
            val aktivitetsloggMediator = AktivitetsloggMediator()

            // Logger bare oppgaver enn så lenge. Bør inn i HendelseMediator
            ArenaOppgaveMottak(rapidsConnection, SakRepository())

            // Start jobb som avbryter behandlinger som står i innvilgelse for lenge
            AvbrytInnvilgelse(rapidsConnection).start()

            val personRepository =
                PersonRepositoryPostgres(
                    BehandlingRepositoryPostgres(
                        OpplysningerRepositoryPostgres(),
                        AvklaringRepositoryPostgres(AvklaringKafkaObservatør(rapidsConnection)),
                    ),
                )

            val hendelseMediator = HendelseMediator(personRepository)
            engine.application.behandlingApi(
                personRepository = personRepository,
                hendelseMediator = hendelseMediator,
                auditlogg = ApiAuditlogg(aktivitetsloggMediator, rapidsConnection),
                opplysningstyper = opplysningstyper,
            ) { ident: String -> ApiMessageContext(rapidsConnection, ident) }

            MessageMediator(
                rapidsConnection = rapidsConnection,
                hendelseMediator = hendelseMediator,
                hendelseRepository = PostgresHendelseRepository(),
                opplysningstyper = opplysningstyper,
            )
        }

    private fun statusPages(): StatusPagesConfig.() -> Unit =
        {
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

    init {
        rapidsConnection.register(this)
    }

    fun start() = rapidsConnection.start()

    fun stop() = rapidsConnection.stop()

    override fun onStartup(rapidsConnection: RapidsConnection) {
        if (config["CLEAN_ON_STARTUP"] == "true") clean()
        runMigration()
        logger.info { "Starter opp dp-behandling. Støtter innvilgelse=$støtterInnvilgelse" }
    }
}
