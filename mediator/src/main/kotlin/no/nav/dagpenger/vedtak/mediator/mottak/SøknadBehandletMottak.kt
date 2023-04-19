package no.nav.dagpenger.vedtak.mediator.mottak

import mu.KotlinLogging
import mu.withLoggingContext
import no.nav.dagpenger.vedtak.mediator.MessageMediator
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River
import java.util.UUID

internal class SøknadBehandletMottak(
    rapidsConnection: RapidsConnection,
    private val messageMediator: MessageMediator,
) :
    River.PacketListener {

    companion object {
        private val logger = KotlinLogging.logger { }
    }

    init {
        River(rapidsConnection).apply {
            validate { it.demandValue("@event_name", "søknad_behandlet_hendelse") }
            validate {
                it.requireKey(
                    "ident",
                    "behandlingId",
                    "virkningsdato",
                    "innvilget",
                )
                it.interestedIn(
                    "dagpengerettighet",
                    "dagsats",
                    "grunnlag",
                    "stønadsperiode",
                    "vanligArbeidstidPerDag",
                    "antallVentedager",
                    "barnetillegg",
                )
            }
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        val behandlingId = UUID.fromString(packet["behandlingId"].asText())
        withLoggingContext("behandlingId" to behandlingId.toString()) {
            val søknadBehandletMessage = SøknadBehandletMessage(packet)
            logger.info { "Fått behandlingshendelse" }
            messageMediator.håndter(søknadBehandletMessage)
        }
    }
}
