package no.nav.dagpenger.vedtak.mediator

import no.nav.dagpenger.vedtak.mediator.mottak.SøknadBehandletMessage
import no.nav.dagpenger.vedtak.mediator.mottak.SøknadBehandletMottak
import no.nav.helse.rapids_rivers.RapidsConnection

internal class MessageMediator(rapidsConnection: RapidsConnection, private val personMediator: PersonMediator) {

    init {
        SøknadBehandletMottak(rapidsConnection, this)
    }

    fun håndter(søknadBehandletMessage: SøknadBehandletMessage) {
        // lagre(søknadBehandletMessage)
        personMediator.håndter(søknadBehandletMessage.hendelse())
        // lagre(søknadBehandletMessage er behandlet)
    }
}
