package no.nav.dagpenger.behandling.mediator

import no.nav.dagpenger.behandling.konfigurasjon.Configuration

fun main() {
    ApplicationBuilder(Configuration.config).start()
}
