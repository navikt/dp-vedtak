package no.nav.dagpenger.vedtak.mediator.vedtak

import no.nav.dagpenger.vedtak.modell.PersonObserver
import no.nav.dagpenger.vedtak.modell.vedtak.VedtakObserver
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.RapidsConnection

internal class VedtakFattetKafkaObserver(private val rapidsConnection: RapidsConnection) : PersonObserver {

    override fun vedtaktFattet(ident: String, vedtakFattet: VedtakObserver.VedtakFattet) {
        val message = JsonMessage.newMessage(
            eventName = "vedtak_fattet",
            map = mapOf(
                "ident" to ident,
                "behandlingId" to vedtakFattet.behandlingId.toString(),
                "vedtakId" to vedtakFattet.vedtakId.toString(),
                "vedtaktidspunkt" to vedtakFattet.vedtakstidspunkt,
                "virkningsdato" to vedtakFattet.virkningsdato,
                "utfall" to vedtakFattet.utfall.name,
            ),
        )

        rapidsConnection.publish(
            key = ident,
            message = message.toJson(),
        )
    }
}
