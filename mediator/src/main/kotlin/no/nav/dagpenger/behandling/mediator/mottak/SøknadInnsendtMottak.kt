package no.nav.dagpenger.behandling.mediator.mottak

import io.opentelemetry.instrumentation.annotations.WithSpan
import mu.KotlinLogging
import mu.withLoggingContext
import no.nav.dagpenger.behandling.mediator.HendelseMediator
import no.nav.dagpenger.behandling.mediator.IHendelseMediator
import no.nav.dagpenger.behandling.mediator.asUUID
import no.nav.dagpenger.behandling.mediator.melding.HendelseMessage
import no.nav.dagpenger.behandling.modell.hendelser.SøknadInnsendtHendelse
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River

internal class SøknadInnsendtMottak(
    rapidsConnection: RapidsConnection,
    private val hendelseMediator: HendelseMediator,
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

    @WithSpan
    override fun onPacket(
        packet: JsonMessage,
        context: MessageContext,
    ) {
        logger.info { "Mottok søknad innsendt hendelse" }
        sikkerlogg.info { "Mottok søknad innsendt hendelse: ${packet.toJson()}" }
        val message = SøknadInnsendtMessage(packet)
        message.behandle(hendelseMediator, context)
    }

    private companion object {
        private val logger = KotlinLogging.logger {}
        private val sikkerlogg = KotlinLogging.logger("tjenestekall.SøknadInnsendtMottak")
    }
}

internal class SøknadInnsendtMessage(private val packet: JsonMessage) : HendelseMessage(packet) {
    override val ident get() = packet["fødselsnummer"].asText()
    private val hendelse
        get() =
            SøknadInnsendtHendelse(
                id,
                ident,
                søknadId = packet["søknadsData"]["søknad_uuid"].asUUID(),
                gjelderDato = java.time.LocalDate.now(),
            )

    override fun behandle(
        mediator: IHendelseMediator,
        context: MessageContext,
    ) {
        withLoggingContext(hendelse.kontekstMap()) {
            logger.info { "Behandler søknad innsendt hendelse" }
            mediator.behandle(hendelse, this, context)
        }
    }

    private companion object {
        private val logger = KotlinLogging.logger {}
    }
}
