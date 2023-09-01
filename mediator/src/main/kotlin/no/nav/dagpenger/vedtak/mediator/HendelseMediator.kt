package no.nav.dagpenger.vedtak.mediator

import no.nav.dagpenger.vedtak.iverksetting.hendelser.IverksattHendelse
import no.nav.dagpenger.vedtak.iverksetting.hendelser.UtbetalingsvedtakFattetHendelse
import no.nav.dagpenger.vedtak.iverksetting.mediator.IverksettingMediator
import no.nav.dagpenger.vedtak.iverksetting.mediator.mottak.IverksattHendelseMessage
import no.nav.dagpenger.vedtak.iverksetting.mediator.mottak.IverksettingLøstMottak
import no.nav.dagpenger.vedtak.iverksetting.mediator.mottak.UtbetalingsvedtakFattetHendelseMessage
import no.nav.dagpenger.vedtak.iverksetting.mediator.mottak.UtbetalingsvedtakFattetMottak
import no.nav.dagpenger.vedtak.mediator.melding.HendelseMessage
import no.nav.dagpenger.vedtak.mediator.melding.HendelseRepository
import no.nav.dagpenger.vedtak.mediator.mottak.RapporteringBehandletHendelseMessage
import no.nav.dagpenger.vedtak.mediator.mottak.RapporteringBehandletMottak
import no.nav.dagpenger.vedtak.mediator.mottak.SøknadBehandletHendelseMessage
import no.nav.dagpenger.vedtak.mediator.mottak.SøknadBehandletMottak
import no.nav.dagpenger.vedtak.modell.hendelser.Hendelse
import no.nav.dagpenger.vedtak.modell.hendelser.Rapporteringshendelse
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
        UtbetalingsvedtakFattetMottak(rapidsConnection, this)
        IverksettingLøstMottak(rapidsConnection, this)
        RapporteringBehandletMottak(rapidsConnection, this)
    }

    override fun behandle(
        hendelse: SøknadBehandletHendelse,
        message: SøknadBehandletHendelseMessage,
        context: MessageContext,
    ) {
        behandle(hendelse, message) {
            personMediator.håndter(it)
        }
    }

    override fun behandle(
        hendelse: IverksattHendelse,
        message: IverksattHendelseMessage,
        context: MessageContext,
    ) {
        behandle(hendelse, message) {
            iverksettingMediator.håndter(it)
        }
    }

    override fun behandle(
        hendelse: Rapporteringshendelse,
        message: RapporteringBehandletHendelseMessage,
        context: MessageContext,
    ) {
        behandle(hendelse, message) {
            personMediator.håndter(it)
        }
    }

    override fun behandle(
        hendelse: UtbetalingsvedtakFattetHendelse,
        message: UtbetalingsvedtakFattetHendelseMessage,
        context: MessageContext,
    ) {
        behandle(hendelse, message) {
            iverksettingMediator.håndter(hendelse)
        }
    }

    private fun <HENDELSE : Hendelse> behandle(
        hendelse: HENDELSE,
        message: HendelseMessage,
        håndter: (HENDELSE) -> Unit,
    ) {
        message.lagreMelding(hendelseRepository)
        håndter(hendelse) // @todo: feilhåndtering
        hendelseRepository.markerSomBehandlet(message.id)
    }
}

internal interface IHendelseMediator {
    fun behandle(hendelse: SøknadBehandletHendelse, message: SøknadBehandletHendelseMessage, context: MessageContext)
    fun behandle(hendelse: IverksattHendelse, message: IverksattHendelseMessage, context: MessageContext)
    fun behandle(
        hendelse: Rapporteringshendelse,
        message: RapporteringBehandletHendelseMessage,
        context: MessageContext,
    )

    fun behandle(
        hendelse: UtbetalingsvedtakFattetHendelse,
        message: UtbetalingsvedtakFattetHendelseMessage,
        context: MessageContext,
    )
}
