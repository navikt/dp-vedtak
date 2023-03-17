package no.nav.dagpenger.vedtak.mediator

import no.nav.dagpenger.vedtak.mediator.meldinger.RettighetsavklaringResultatService
import no.nav.helse.rapids_rivers.RapidApplication
import no.nav.helse.rapids_rivers.RapidsConnection

internal class ApplicationBuilder(config: Map<String, String>) : RapidsConnection.StatusListener {

    private val rapidsConnection = RapidApplication.Builder(
        RapidApplication.RapidApplicationConfig.fromEnv(config),
    ).build()

    private val rettighetsavklaringResultatService = RettighetsavklaringResultatService(rapidsConnection)

    init {
        rapidsConnection.register(this)
    }

    fun start() = rapidsConnection.start()
    fun stop() = rapidsConnection.stop()

    override fun onStartup(rapidsConnection: RapidsConnection) {
        // logg.info { "Starter dp-mottak" }
    }
/*
    private fun subscribe(meldingObserver: MeldingObserver) {
        mediator.register(meldingObserver)
    }

    private fun publiser(fnr: String) {
        mediator.nySøknad(fnr)
    } */
}
