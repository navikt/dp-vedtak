package no.nav.dagpenger.vedtak.mediator

import no.nav.dagpenger.vedtak.iverksetting.hendelser.IverksattHendelse
import no.nav.dagpenger.vedtak.iverksetting.hendelser.VedtakFattetHendelse
import no.nav.dagpenger.vedtak.iverksetting.mediator.IverksettingMediator
import no.nav.dagpenger.vedtak.iverksetting.mediator.mottak.IverksattHendelseMessage
import no.nav.dagpenger.vedtak.iverksetting.mediator.mottak.IverksettingLøstMottak
import no.nav.dagpenger.vedtak.iverksetting.mediator.mottak.VedtakFattetHendelseMessage
import no.nav.dagpenger.vedtak.iverksetting.mediator.mottak.VedtakFattetMottak
import no.nav.dagpenger.vedtak.mediator.melding.HendelseRepository
import no.nav.dagpenger.vedtak.mediator.mottak.SøknadBehandletHendelseMessage
import no.nav.dagpenger.vedtak.mediator.mottak.SøknadBehandletMottak
import no.nav.dagpenger.vedtak.modell.hendelser.SøknadBehandletHendelse
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.RapidsConnection

internal class HendelseMediator(
    rapidsConnection: RapidsConnection,
    private val personMediator: PersonMediator,
    private val iverksettingMediator: IverksettingMediator,
    private val hendelseRepository: HendelseRepository,
) : IHendelseMediator {

    init {
        SøknadBehandletMottak(rapidsConnection, this)
        VedtakFattetMottak(rapidsConnection, this)
        IverksettingLøstMottak(rapidsConnection, this)
    }

    override fun behandle(
        melding: SøknadBehandletHendelse,
        hendelse: SøknadBehandletHendelseMessage,
        context: MessageContext,
    ) {
        hendelse.lagreMelding(hendelseRepository)
        personMediator.håndter(melding)
        hendelseRepository.markerSomBehandlet(hendelse.id)
    }

    override fun behandle(
        melding: VedtakFattetHendelse,
        hendelse: VedtakFattetHendelseMessage,
        context: MessageContext,
    ) {
        hendelse.lagreMelding(hendelseRepository)
        iverksettingMediator.håndter(melding)
        hendelseRepository.markerSomBehandlet(hendelse.id)
    }

    override fun behandle(
        melding: IverksattHendelse,
        hendelse: IverksattHendelseMessage,
        context: MessageContext,
    ) {
        hendelse.lagreMelding(hendelseRepository)
        iverksettingMediator.håndter(melding)
        hendelseRepository.markerSomBehandlet(hendelse.id)
    }
}

internal interface IHendelseMediator {
    fun behandle(melding: SøknadBehandletHendelse, hendelse: SøknadBehandletHendelseMessage, context: MessageContext)
    fun behandle(melding: VedtakFattetHendelse, hendelse: VedtakFattetHendelseMessage, context: MessageContext)
    fun behandle(melding: IverksattHendelse, hendelse: IverksattHendelseMessage, context: MessageContext)
}
