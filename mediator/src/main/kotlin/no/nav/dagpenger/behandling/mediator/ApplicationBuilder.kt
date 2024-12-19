package no.nav.dagpenger.behandling.mediator

import com.github.navikt.tbd_libs.naisful.naisApp
import com.github.navikt.tbd_libs.rapids_and_rivers.KafkaRapid
import com.github.navikt.tbd_libs.rapids_and_rivers_api.RapidsConnection
import io.micrometer.core.instrument.Clock
import io.micrometer.prometheusmetrics.PrometheusConfig
import io.micrometer.prometheusmetrics.PrometheusMeterRegistry
import io.prometheus.metrics.model.registry.PrometheusRegistry
import mu.KotlinLogging
import no.nav.dagpenger.behandling.db.PostgresDataSourceBuilder.runMigration
import no.nav.dagpenger.behandling.konfigurasjon.støtterInnvilgelse
import no.nav.dagpenger.behandling.mediator.api.ApiMessageContext
import no.nav.dagpenger.behandling.mediator.api.behandlingApi
import no.nav.dagpenger.behandling.mediator.audit.ApiAuditlogg
import no.nav.dagpenger.behandling.mediator.jobber.AvbrytInnvilgelse
import no.nav.dagpenger.behandling.mediator.jobber.SlettFjernetOpplysninger
import no.nav.dagpenger.behandling.mediator.melding.PostgresHendelseRepository
import no.nav.dagpenger.behandling.mediator.mottak.ArenaOppgaveMottak
import no.nav.dagpenger.behandling.mediator.mottak.SakRepositoryPostgres
import no.nav.dagpenger.behandling.mediator.repository.AvklaringKafkaObservatør
import no.nav.dagpenger.behandling.mediator.repository.AvklaringRepositoryPostgres
import no.nav.dagpenger.behandling.mediator.repository.BehandlingRepositoryPostgres
import no.nav.dagpenger.behandling.mediator.repository.OpplysningerRepositoryPostgres
import no.nav.dagpenger.behandling.mediator.repository.PersonRepositoryPostgres
import no.nav.dagpenger.behandling.mediator.repository.VaktmesterPostgresRepo
import no.nav.dagpenger.behandling.objectMapper
import no.nav.dagpenger.opplysning.Opplysningstype
import no.nav.dagpenger.regel.RegelverkDagpenger
import no.nav.helse.rapids_rivers.RapidApplication

internal class ApplicationBuilder(
    config: Map<String, String>,
) : RapidsConnection.StatusListener {
    companion object {
        private val logger = KotlinLogging.logger { }
    }

    // TODO: Last alle regler ved startup. Dette må inn i ett register.
    private val opplysningstyper: Set<Opplysningstype<*>> = RegelverkDagpenger.produserer
    private val rapidsConnection: RapidsConnection =
        RapidApplication.create(
            env = config,
            builder = {
                withKtor { preStopHook, rapid ->
                    naisApp(
                        meterRegistry =
                            PrometheusMeterRegistry(
                                PrometheusConfig.DEFAULT,
                                PrometheusRegistry.defaultRegistry,
                                Clock.SYSTEM,
                            ),
                        objectMapper = objectMapper,
                        applicationLogger = KotlinLogging.logger("ApplicationLogger"),
                        callLogger = KotlinLogging.logger("CallLogger"),
                        aliveCheck = rapid::isReady,
                        readyCheck = rapid::isReady,
                        preStopHook = preStopHook::handlePreStopRequest,
                    ) {
                        val personRepository =
                            PersonRepositoryPostgres(
                                BehandlingRepositoryPostgres(
                                    OpplysningerRepositoryPostgres(),
                                    AvklaringRepositoryPostgres(AvklaringKafkaObservatør(rapid)),
                                ),
                            )

                        val hendelseMediator = HendelseMediator(personRepository)

                        MessageMediator(
                            rapidsConnection = rapid,
                            hendelseMediator = hendelseMediator,
                            hendelseRepository = PostgresHendelseRepository(),
                            opplysningstyper = opplysningstyper,
                        )

                        behandlingApi(
                            personRepository = personRepository,
                            hendelseMediator = hendelseMediator,
                            auditlogg = ApiAuditlogg(AktivitetsloggMediator(), rapid),
                            opplysningstyper = opplysningstyper,
                        ) { ident: String -> ApiMessageContext(rapid, ident) }
                    }
                }
            },
        ) { _, rapidsConnection: KafkaRapid ->
            // Logger bare oppgaver enn så lenge. Bør inn i HendelseMediator
            ArenaOppgaveMottak(rapidsConnection, SakRepositoryPostgres())

            // Start jobb som avbryter behandlinger som står i innvilgelse for lenge
            AvbrytInnvilgelse(rapidsConnection).start(config["AVBRYT_INNVILGELSE_ETTER_DAGER"]?.toInt() ?: 3)

            // Start jobb som sletter fjernet opplysninger
            SlettFjernetOpplysninger.slettOpplysninger(VaktmesterPostgresRepo())
        }

    init {
        rapidsConnection.register(this)
    }

    fun start() = rapidsConnection.start()

    fun stop() = rapidsConnection.stop()

    override fun onStartup(rapidsConnection: RapidsConnection) {
        runMigration()
        logger.info { "Starter opp dp-behandling. Støtter innvilgelse=$støtterInnvilgelse" }
    }
}
