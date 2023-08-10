package no.nav.dagpenger.vedtak.mediator

import mu.KotlinLogging
import no.nav.dagpenger.vedtak.db.PostgresDataSourceBuilder
import no.nav.dagpenger.vedtak.db.PostgresDataSourceBuilder.clean
import no.nav.dagpenger.vedtak.db.PostgresDataSourceBuilder.runMigration
import no.nav.dagpenger.vedtak.iverksetting.mediator.IverksettingMediator
import no.nav.dagpenger.vedtak.iverksetting.mediator.persistens.PostgresIverksettingRepository
import no.nav.dagpenger.vedtak.mediator.api.vedtakApi
import no.nav.dagpenger.vedtak.mediator.persistens.PostgresHendelseRepository
import no.nav.dagpenger.vedtak.mediator.persistens.PostgresPersonRepository
import no.nav.dagpenger.vedtak.mediator.vedtak.VedtakFattetKafkaObserver
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
            hendelseRepository = PostgresHendelseRepository(PostgresDataSourceBuilder.dataSource),
            personMediator = PersonMediator(
                aktivitetsloggMediator = AktivitetsloggMediator(rapidsConnection),
                personRepository = personRepository,
                personObservers = listOf(
                    VedtakFattetKafkaObserver(rapidsConnection),
                ),
            ),
            iverksettingMediator = IverksettingMediator(
                aktivitetsloggMediator = AktivitetsloggMediator(rapidsConnection),
                iverksettingRepository = PostgresIverksettingRepository(PostgresDataSourceBuilder.dataSource),
                behovMediator = BehovMediator(rapidsConnection, KotlinLogging.logger("tjenestekall.BehovMediator")),
            ),
        )

        rapidsConnection.register(this)
    }

    fun start() = rapidsConnection.start()
    fun stop() = rapidsConnection.stop()

    override fun onStartup(rapidsConnection: RapidsConnection) {
        clean()
        runMigration()
        logger.info { "Starter opp dp-vedtak" }
    }
}
