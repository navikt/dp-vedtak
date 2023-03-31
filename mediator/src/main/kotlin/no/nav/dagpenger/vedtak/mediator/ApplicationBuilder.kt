package no.nav.dagpenger.vedtak.mediator

import mu.KotlinLogging
import no.nav.dagpenger.vedtak.mediator.persistens.InMemoryPersonRepository
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
        PersonMediator(rapidsConnection = rapidsConnection, personRepository = InMemoryPersonRepository())
        rapidsConnection.register(this)
    }

    fun start() = rapidsConnection.start()
    fun stop() = rapidsConnection.stop()

    override fun onStartup(rapidsConnection: RapidsConnection) {
        logger.info { "Starter opp dp-vedtak" }
    }
}
