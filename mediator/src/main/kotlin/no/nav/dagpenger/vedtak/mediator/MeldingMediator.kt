package no.nav.dagpenger.vedtak.mediator

import no.nav.dagpenger.vedtak.mediator.mottak.SøknadBehandletMelding
import no.nav.dagpenger.vedtak.mediator.mottak.SøknadBehandletMottak
import no.nav.dagpenger.vedtak.mediator.persistens.InMemoryMeldingRepository
import no.nav.helse.rapids_rivers.RapidsConnection

internal class MeldingMediator(
    rapidsConnection: RapidsConnection,
    private val personMediator: PersonMediator,
    private val meldingRepository: InMemoryMeldingRepository,
) {

    init {
        SøknadBehandletMottak(rapidsConnection, this)
    }

    fun håndter(søknadBehandletMelding: SøknadBehandletMelding) {
        meldingRepository.lagre(søknadBehandletMelding)
        personMediator.håndter(søknadBehandletMelding.hendelse())
        // lagre(søknadBehandletMessage er behandlet)
    }
}
