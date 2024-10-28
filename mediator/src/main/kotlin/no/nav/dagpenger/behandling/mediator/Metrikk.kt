package no.nav.dagpenger.behandling.mediator

import io.prometheus.metrics.core.metrics.Histogram
import io.prometheus.metrics.model.registry.PrometheusRegistry

internal object Metrikk {
    private val antallBehandlinger =
        Histogram
            .builder()
            .name("dp_antall_behandlinger")
            .help("Antall behandlinger per person")
            .register(PrometheusRegistry.defaultRegistry)

    fun registrerAntallBehandlinger(antall: Int) {
        antallBehandlinger.observe(antall.toDouble())
    }
}
