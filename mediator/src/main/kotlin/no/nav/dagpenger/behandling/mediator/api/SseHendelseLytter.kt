package no.nav.dagpenger.behandling.mediator.api

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.runBlocking
import no.nav.dagpenger.behandling.mediator.asUUID
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River

class SseHendelseLytter(
    rapidsConnection: RapidsConnection,
) : River.PacketListener {
    init {
        River(rapidsConnection)
            .apply {
                validate { it.requireValue("@event_name", "behandling_endret_tilstand") }
                validate { it.requireKey("behandlingId", "gjeldendeTilstand") }
            }.register(this)
    }

    private val hendelser = MutableSharedFlow<BehandlingSseEvent>() // private mutable shared flow

    fun hendelser() = hendelser.asSharedFlow()

    override fun onPacket(
        packet: JsonMessage,
        context: MessageContext,
    ) {
        val behandlingSseEvent =
            BehandlingSseEvent(
                behandlingId = packet["behandlingId"].asUUID(),
                handling =
                    """
                    Endret tilstand til ${packet["gjeldendeTilstand"].asText()}
                    """.trimIndent(),
            )

        runBlocking { hendelser.emit(behandlingSseEvent) }
    }
}
