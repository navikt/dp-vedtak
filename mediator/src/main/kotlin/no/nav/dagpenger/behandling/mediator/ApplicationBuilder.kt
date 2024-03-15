package no.nav.dagpenger.behandling.mediator

import mu.KotlinLogging
import no.nav.dagpenger.behandling.db.PostgresDataSourceBuilder
import no.nav.dagpenger.behandling.db.PostgresDataSourceBuilder.clean
import no.nav.dagpenger.behandling.db.PostgresDataSourceBuilder.runMigration
import no.nav.dagpenger.behandling.mediator.api.behandlingApi
import no.nav.dagpenger.behandling.mediator.melding.PostgresHendelseRepository
import no.nav.dagpenger.behandling.mediator.repository.BehandlingRepositoryPostgres
import no.nav.dagpenger.behandling.mediator.repository.OpplysningerRepositoryPostgres
import no.nav.dagpenger.behandling.mediator.repository.PersonRepositoryPostgres
import no.nav.helse.rapids_rivers.RapidApplication
import no.nav.helse.rapids_rivers.RapidsConnection

internal class ApplicationBuilder(config: Map<String, String>) : RapidsConnection.StatusListener {
    companion object {
        private val logger = KotlinLogging.logger { }
    }

    private val opplysningRepository = OpplysningerRepositoryPostgres()
    private val behandlingRepository = BehandlingRepositoryPostgres(opplysningRepository)
    private val personRepository = PersonRepositoryPostgres(behandlingRepository)
    private val rapidsConnection =
        RapidApplication.Builder(RapidApplication.RapidApplicationConfig.fromEnv(config))
            .withKtorModule { behandlingApi(personRepository = personRepository) }.build()

    init {
        MessageMediator(
            rapidsConnection = rapidsConnection,
            personMediator =
                PersonMediator(
                    personRepository = personRepository,
                    aktivitetsloggMediator = AktivitetsloggMediator(rapidsConnection),
                    behovMediator = BehovMediator(rapidsConnection),
                    hendelseMediator = HendelseMediator(rapidsConnection),
                    observatører = emptySet(),
                ),
            hendelseRepository = PostgresHendelseRepository(PostgresDataSourceBuilder.dataSource),
        )

        rapidsConnection.register(this)
    }

    fun start() = rapidsConnection.start()

    fun stop() = rapidsConnection.stop()

    override fun onStartup(rapidsConnection: RapidsConnection) {
        clean()
        runMigration()
        logger.info { "Starter opp dp-behandling" }
    }
}
