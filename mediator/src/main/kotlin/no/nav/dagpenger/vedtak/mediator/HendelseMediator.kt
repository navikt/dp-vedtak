package no.nav.dagpenger.vedtak.mediator

import no.nav.dagpenger.vedtak.mediator.mottak.SøknadBehandletHendelseMessage
import no.nav.dagpenger.vedtak.mediator.mottak.SøknadBehandletMottak
import no.nav.dagpenger.vedtak.mediator.persistens.InMemoryMeldingRepository
import no.nav.helse.rapids_rivers.RapidsConnection

internal class HendelseMediator(
    rapidsConnection: RapidsConnection,
    private val personMediator: PersonMediator,
    private val meldingRepository: InMemoryMeldingRepository,
) {

    init {
        SøknadBehandletMottak(rapidsConnection, this)
    }

    fun håndter(søknadBehandletMelding: SøknadBehandletHendelseMessage) {
        meldingRepository.lagre(søknadBehandletMelding)
        try {
            personMediator.håndter(søknadBehandletMelding.hendelse())
            meldingRepository.behandlet(søknadBehandletMelding)
        } catch (e: Throwable) {
            meldingRepository.feilet(søknadBehandletMelding)
            throw e
        }
    }
}
