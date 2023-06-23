package no.nav.dagpenger.vedtak.mediator

import mu.KotlinLogging
import no.nav.dagpenger.vedtak.iverksetting.mediator.InMemoryIverksettingRepository
import no.nav.dagpenger.vedtak.iverksetting.mediator.IverksettingMediator
import no.nav.dagpenger.vedtak.mediator.persistens.InMemoryMeldingRepository
import no.nav.dagpenger.vedtak.mediator.persistens.InMemoryPersonRepository
import no.nav.dagpenger.vedtak.mediator.vedtak.VedtakFattetKafkaObserver
import no.nav.helse.rapids_rivers.RapidApplication
import no.nav.helse.rapids_rivers.RapidsConnection

internal class ApplicationBuilder(config: Map<String, String>) : RapidsConnection.StatusListener {

    companion object {
        private val logger = KotlinLogging.logger { }
    }

    private val rapidsConnection = RapidApplication.Builder(
        RapidApplication.RapidApplicationConfig.fromEnv(config),
    ).build()

    init {
        HendelseMediator(
            rapidsConnection = rapidsConnection,
            hendelseRepository = InMemoryMeldingRepository(),
            personMediator = PersonMediator(
                personRepository = InMemoryPersonRepository(),
                personObservers = listOf(
                    VedtakFattetKafkaObserver(rapidsConnection),
                ),
            ),
            iverksettingMediator = IverksettingMediator(
                iverksettingRepository = InMemoryIverksettingRepository(),
                behovMediator = BehovMediator(rapidsConnection, KotlinLogging.logger("tjenestekall.BehovMediator")),
            ),
        )
        /*
        IverksettBehovløser(
            rapidsConnection = rapidsConnection,
            iverksettClient = IverksettClient(
                Configuration.iverksettApiUrl,
                Configuration.iverksettClientTokenSupplier,
            ),
        )*/

        rapidsConnection.register(this)
    }

    fun start() = rapidsConnection.start()
    fun stop() = rapidsConnection.stop()

    override fun onStartup(rapidsConnection: RapidsConnection) {
        logger.info { "Starter opp dp-vedtak" }
    }
}
