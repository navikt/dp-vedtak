package no.nav.dagpenger.vedtak.mediator

import mu.KotlinLogging
import no.nav.dagpenger.vedtak.db.PostgresDataSourceBuilder
import no.nav.dagpenger.vedtak.db.PostgresDataSourceBuilder.runMigration
import no.nav.dagpenger.vedtak.mediator.api.vedtakApi
import no.nav.dagpenger.vedtak.mediator.persistens.PostgresHendelseRepository
import no.nav.dagpenger.vedtak.mediator.persistens.PostgresPersonRepository
import no.nav.dagpenger.vedtak.mediator.vedtak.VedtakFattetObserver
import no.nav.helse.rapids_rivers.RapidApplication
import no.nav.helse.rapids_rivers.RapidsConnection

internal class ApplicationBuilder(config: Map<String, String>) : RapidsConnection.StatusListener {

    companion object {
        private val logger = KotlinLogging.logger { }
    }

    private val personRepository = PostgresPersonRepository(PostgresDataSourceBuilder.dataSource)

    private val rapidsConnection = RapidApplication.Builder(
        RapidApplication.RapidApplicationConfig.fromEnv(config),
    )
        .withKtorModule { vedtakApi(personRepository = personRepository) }
        .build()

    init {
        HendelseMediator(
            rapidsConnection = rapidsConnection,
            personMediator = PersonMediator(
                aktivitetsloggMediator = AktivitetsloggMediator(rapidsConnection),
                personRepository = personRepository,
                personObservers = listOf(
                    VedtakFattetObserver(rapidsConnection),
                ),
            ),
            hendelseRepository = PostgresHendelseRepository(PostgresDataSourceBuilder.dataSource),
        )

        rapidsConnection.register(this)
    }

    fun start() = rapidsConnection.start()
    fun stop() = rapidsConnection.stop()

    override fun onStartup(rapidsConnection: RapidsConnection) {
        runMigration()
        logger.info { "Starter opp dp-vedtak" }
    }
}
