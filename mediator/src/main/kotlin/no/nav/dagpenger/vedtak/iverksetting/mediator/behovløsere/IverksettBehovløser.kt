package no.nav.dagpenger.vedtak.iverksetting.mediator.behovløsere

import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.RapidsConnection
import no.nav.helse.rapids_rivers.River

class IverksettBehovløser(rapidsConnection: RapidsConnection) : River.PacketListener {

    init {
        River(rapidsConnection).apply {
            validate { it.requireValue("@event_type", "behov") }
            validate { it.demandAllOrAny("@behov", listOf("Iverksett")) }
            validate({
                it.require("Iverksett", { Iverksett ->
                    Iverksett.required("vedtakId")
                    Iverksett.required("behandlingId")
                    Iverksett.required("vedtakstidspunkt")
                    Iverksett.required("virkningsdato")
                    Iverksett.required("utfall")
                })
            })
        }.register(this)
    }
    override fun onPacket(packet: JsonMessage, context: MessageContext) {
        TODO("Not yet implemented")
    }
}
