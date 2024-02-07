package no.nav.dagpenger.vedtak.mediator.mottak

import mu.KotlinLogging
import no.nav.dagpenger.vedtak.mediator.HendelseMediator
import no.nav.dagpenger.vedtak.mediator.IHendelseMediator
import no.nav.dagpenger.vedtak.mediator.asUUID
import no.nav.dagpenger.vedtak.mediator.melding.HendelseMessage
import no.nav.dagpenger.vedtak.modell.hendelser.SøknadInnsendtHendelse
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.MessageProblems
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River

internal class SøknadInnsendtMottak(
    rapidsConnection: RapidsConnection,
    private val hendelsemediator: HendelseMediator,
) : River.PacketListener {
    init {
        River(rapidsConnection).apply {
            validate { it.demandValue("@event_name", "innsending_ferdigstilt") }
            validate { it.demandAny("type", listOf("NySøknad")) }
            validate { it.requireKey("fødselsnummer") }
            validate {
                it.require("søknadsData") { data ->
                    data["søknad_uuid"].asUUID()
                }
            }
            validate { it.interestedIn("@id", "@opprettet") }
        }.register(this)
    }

    override fun onPacket(
        packet: JsonMessage,
        context: MessageContext,
    ) {
        val message = SøknadInnsendtMessage(packet)
        message.behandle(hendelsemediator, context)
    }

    override fun onError(
        problems: MessageProblems,
        context: MessageContext,
    ) {
        logger.error { problems }
    }

    private companion object {
        private val logger = KotlinLogging.logger {}
    }
}

internal class SøknadInnsendtMessage(private val packet: JsonMessage) : HendelseMessage(packet) {
    private val hendelse
        get() =
            SøknadInnsendtHendelse(
                id,
                ident,
                gjelderDato = java.time.LocalDate.now(),
                søknadId = packet["søknadsData"]["søknad_uuid"].asUUID(),
            )
    override val ident get() = packet["fødselsnummer"].asText()

    override fun behandle(
        mediator: IHendelseMediator,
        context: MessageContext,
    ) {
        mediator.behandle(hendelse, this, context)
    }
}
