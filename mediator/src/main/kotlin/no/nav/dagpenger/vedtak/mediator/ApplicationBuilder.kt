package no.nav.dagpenger.vedtak.mediator

import mu.KotlinLogging
import no.nav.dagpenger.vedtak.mediator.persistens.InMemoryMeldingRepository
import no.nav.dagpenger.vedtak.mediator.persistens.InMemoryPersonRepository
import no.nav.dagpenger.vedtak.mediator.vedtak.VedtakFattetKafkaObserver
import no.nav.dagpenger.vedtak.mediator.vedtak.iverksett.IverksettClient
import no.nav.dagpenger.vedtak.mediator.vedtak.iverksett.VedtakFattetIverksettObserver
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
            meldingRepository = InMemoryMeldingRepository(),
            personMediator = PersonMediator(
                personRepository = InMemoryPersonRepository(),
                personObservers = listOf(
                    VedtakFattetKafkaObserver(rapidsConnection),
                    VedtakFattetIverksettObserver(
                        iverksettClient = IverksettClient(Configuration.iverksettApiUrl, Configuration.iverksettClientTokenSupplier),
                    ),
                ),
            ),
        )
        rapidsConnection.register(this)
    }

    fun start() = rapidsConnection.start()
    fun stop() = rapidsConnection.stop()

    override fun onStartup(rapidsConnection: RapidsConnection) {
        logger.info { "Starter opp dp-vedtak" }
    }
}
