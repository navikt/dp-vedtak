package no.nav.dagpenger.vedtak.mediator

import no.nav.dagpenger.vedtak.iverksetting.mediator.IverksettingMediator
import no.nav.dagpenger.vedtak.iverksetting.mediator.mottak.VedtakFattetHendelseMessage
import no.nav.dagpenger.vedtak.iverksetting.mediator.mottak.VedtakFattetMottak
import no.nav.dagpenger.vedtak.mediator.mottak.SøknadBehandletHendelseMessage
import no.nav.dagpenger.vedtak.mediator.mottak.SøknadBehandletMottak
import no.nav.dagpenger.vedtak.mediator.persistens.InMemoryMeldingRepository
import no.nav.helse.rapids_rivers.RapidsConnection

internal class HendelseMediator(
    rapidsConnection: RapidsConnection,
    private val personMediator: PersonMediator,
    private val iverksettingMediator: IverksettingMediator,
    private val meldingRepository: InMemoryMeldingRepository,
) {

    init {
        SøknadBehandletMottak(rapidsConnection, this)
        VedtakFattetMottak(rapidsConnection, iverksettingMediator)
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

    fun håndter(vedtakFattetHendelseMessage: VedtakFattetHendelseMessage) {
        meldingRepository.lagre(vedtakFattetHendelseMessage)
        try {
            iverksettingMediator.håndter(vedtakFattetHendelseMessage.hendelse())
            meldingRepository.behandlet(vedtakFattetHendelseMessage)
        } catch (e: Throwable) {
            meldingRepository.feilet(vedtakFattetHendelseMessage)
            throw e
        }
    }
}
