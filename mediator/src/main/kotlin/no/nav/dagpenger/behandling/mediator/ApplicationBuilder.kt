package no.nav.dagpenger.behandling.mediator

import mu.KotlinLogging
import no.nav.dagpenger.behandling.db.PostgresDataSourceBuilder.clean
import no.nav.dagpenger.behandling.db.PostgresDataSourceBuilder.runMigration
import no.nav.dagpenger.behandling.mediator.Configuration.config
import no.nav.dagpenger.behandling.mediator.api.behandlingApi
import no.nav.dagpenger.behandling.mediator.audit.AktivitetsloggAuditlogg
import no.nav.dagpenger.behandling.mediator.melding.PostgresHendelseRepository
import no.nav.dagpenger.behandling.mediator.repository.BehandlingRepositoryPostgres
import no.nav.dagpenger.behandling.mediator.repository.OpplysningerRepositoryPostgres
import no.nav.dagpenger.behandling.mediator.repository.PersonRepositoryPostgres
import no.nav.dagpenger.regel.Alderskrav
import no.nav.dagpenger.regel.Meldeplikt
import no.nav.dagpenger.regel.Minsteinntekt
import no.nav.dagpenger.regel.Opptjeningstid
import no.nav.dagpenger.regel.ReellArbeidssøker
import no.nav.dagpenger.regel.RettTilDagpenger
import no.nav.dagpenger.regel.Rettighetstype
import no.nav.dagpenger.regel.Søknadstidspunkt
import no.nav.helse.rapids_rivers.RapidApplication
import no.nav.helse.rapids_rivers.RapidsConnection

internal class ApplicationBuilder(config: Map<String, String>) : RapidsConnection.StatusListener {
    companion object {
        private val logger = KotlinLogging.logger { }
    }

    private val opplysningRepository = OpplysningerRepositoryPostgres()
    private val behandlingRepository = BehandlingRepositoryPostgres(opplysningRepository)
    private val personRepository = PersonRepositoryPostgres(behandlingRepository)
    private val rapidsConnection: RapidsConnection =
        RapidApplication.Builder(RapidApplication.RapidApplicationConfig.fromEnv(config))
            .withKtorModule {
                behandlingApi(
                    personRepository = personRepository,
                    personMediator,
                    AktivitetsloggAuditlogg(aktivitetsloggMediator),
                )
            }.build()

    private val aktivitetsloggMediator = AktivitetsloggMediator(rapidsConnection)
    private val personMediator: PersonMediator =
        PersonMediator(
            personRepository = personRepository,
            aktivitetsloggMediator = aktivitetsloggMediator,
            behovMediator = BehovMediator(rapidsConnection),
            hendelseMediator = HendelseMediator(rapidsConnection),
            observatører = emptySet(),
        )

    init {
        MessageMediator(
            rapidsConnection = rapidsConnection,
            personMediator = personMediator,
            hendelseRepository = PostgresHendelseRepository(),
        )

        rapidsConnection.register(this)
    }

    // TODO: Last alle regler ved startup. Dette må inn i ett register.
    val regler =
        listOf(
            Alderskrav.regelsett,
            Meldeplikt.regelsett,
            Minsteinntekt.regelsett,
            Opptjeningstid.regelsett,
            ReellArbeidssøker.regelsett,
            RettTilDagpenger.regelsett,
            Rettighetstype.regelsett,
            Søknadstidspunkt.regelsett,
        )

    fun start() = rapidsConnection.start()

    fun stop() = rapidsConnection.stop()

    override fun onStartup(rapidsConnection: RapidsConnection) {
        if (config["CLEAN_ON_STARTUP"] == "true") clean()
        runMigration()
        logger.info { "Starter opp dp-behandling" }
    }
}
