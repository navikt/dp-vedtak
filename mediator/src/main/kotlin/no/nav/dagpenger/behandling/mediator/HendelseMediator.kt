package no.nav.dagpenger.behandling.mediator

import com.fasterxml.jackson.databind.JsonNode
import no.nav.dagpenger.behandling.mediator.melding.HendelseMessage
import no.nav.dagpenger.behandling.mediator.melding.HendelseRepository
import no.nav.dagpenger.behandling.mediator.mottak.OpplysningSvarMessage
import no.nav.dagpenger.behandling.mediator.mottak.OpplysningSvarMottak
import no.nav.dagpenger.behandling.mediator.mottak.SøknadInnsendtMessage
import no.nav.dagpenger.behandling.mediator.mottak.SøknadInnsendtMottak
import no.nav.dagpenger.behandling.modell.hendelser.OpplysningSvarHendelse
import no.nav.dagpenger.behandling.modell.hendelser.PersonHendelse
import no.nav.dagpenger.behandling.modell.hendelser.SøknadInnsendtHendelse
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.withMDC
import java.util.UUID

internal class HendelseMediator(
    rapidsConnection: RapidsConnection,
    private val personMediator: PersonMediator,
    private val hendelseRepository: HendelseRepository,
) : IHendelseMediator {
    init {
        SøknadInnsendtMottak(rapidsConnection, this)
        OpplysningSvarMottak(rapidsConnection, this)
    }

    override fun behandle(
        hendelse: SøknadInnsendtHendelse,
        message: SøknadInnsendtMessage,
        context: MessageContext,
    ) {
        behandle(hendelse, message) {
            personMediator.håndter(it)
        }
    }

    override fun behandle(
        hendelse: OpplysningSvarHendelse,
        message: OpplysningSvarMessage,
        context: MessageContext,
    ) {
        behandle(hendelse, message) {
            personMediator.håndter(it)
        }
    }

    private fun <HENDELSE : PersonHendelse> behandle(
        hendelse: HENDELSE,
        message: HendelseMessage,
        håndter: (HENDELSE) -> Unit,
    ) {
        withMDC(message.tracinginfo()) {
            message.lagreMelding(hendelseRepository)
            håndter(hendelse) // @todo: feilhåndtering
            hendelseRepository.markerSomBehandlet(message.id)
        }
    }
}

internal interface IHendelseMediator {
    fun behandle(
        hendelse: SøknadInnsendtHendelse,
        message: SøknadInnsendtMessage,
        context: MessageContext,
    )

    fun behandle(
        hendelse: OpplysningSvarHendelse,
        message: OpplysningSvarMessage,
        context: MessageContext,
    )
}

fun JsonNode.asUUID(): UUID = this.asText().let { UUID.fromString(it) }
