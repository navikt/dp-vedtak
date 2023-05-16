package no.nav.dagpenger.vedtak.mediator.mottak

import mu.KotlinLogging
import no.nav.dagpenger.vedtak.mediator.IHendelseMediator
import no.nav.dagpenger.vedtak.modell.PersonIdentifikator
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.MessageProblems
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River

internal class RapporteringBehandletMottak(
    rapidsConnection: RapidsConnection,
    private val hendelseMediator: IHendelseMediator,
) :
    River.PacketListener {

    companion object {
        private val logger = KotlinLogging.logger { }
    }
    init {
        River(rapidsConnection).apply {
            validate { it.demandValue("@event_name", "rapportering_behandlet_hendelse") }
            validate {
                it.requireKey("@id", "@opprettet")
                it.require("ident") { ident ->
                    PersonIdentifikator(ident.asText())
                }
                it.requireKey(
                    "behandlingId",
                    "periodeId",
                    "virkningsdato",
                    "innvilget",
                    "dager",
                )
            }
        }.register(this)
    }

    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        TODO("Not yet implemented")
    }

    override fun onError(problems: MessageProblems, context: MessageContext) {
        throw RuntimeException(problems.toExtendedReport())
    }
}
