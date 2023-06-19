package no.nav.dagpenger.vedtak.mediator.mottak

import mu.KotlinLogging
import mu.withLoggingContext
import no.nav.dagpenger.vedtak.mediator.IHendelseMediator
import no.nav.dagpenger.vedtak.modell.PersonIdentifikator
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River
import java.util.UUID

internal class RapporteringBehandletMottak(
    rapidsConnection: RapidsConnection,
    private val hendelseMediator: IHendelseMediator,
) :
    River.PacketListener {

    companion object {
        private val logger = KotlinLogging.logger { }
        private val sikkerLogger = KotlinLogging.logger("tjenestekall.RapporteringBehandletMottak")
    }

    init {
        River(rapidsConnection).apply {
            validate { it.demandValue("@event_name", "rapporteringsperiode_innsendt_hendelse") }
            validate {
                it.requireKey("@id", "@opprettet")
                it.require("ident") { ident ->
                    PersonIdentifikator(ident.asText())
                }
                it.requireKey(
                    "rapporteringsId",
                    "fom",
                    "tom",
                    "dager",
                )
            }
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        val rapporteringsId = UUID.fromString(packet["rapporteringsId"].asText())
        withLoggingContext("rapporteringsId" to rapporteringsId.toString()) {
            val rapporteringBehandletHendelseMessage = RapporteringBehandletHendelseMessage(packet)
            logger.info { "Fått rapportering innsendt hendelse" }
            sikkerLogger.info { "Fått rapportering innsendt hendelse: ${packet.toJson()}" }
            rapporteringBehandletHendelseMessage.behandle(hendelseMediator, context)
        }
    }
}
