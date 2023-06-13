package no.nav.dagpenger.vedtak.mediator.vedtak

import mu.KotlinLogging
import no.nav.dagpenger.vedtak.iverksetting.mediator.behovløsere.behandlingId
import no.nav.dagpenger.vedtak.modell.PersonObserver
import no.nav.dagpenger.vedtak.modell.vedtak.VedtakObserver
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.RapidsConnection

internal class VedtakFattetKafkaObserver(private val rapidsConnection: RapidsConnection) : PersonObserver {

    companion object {
        val logger = KotlinLogging.logger { }
        val sikkerlogger = KotlinLogging.logger { "tjenestekall.VedtakFattetKafkaObserver" }
    }

    override fun vedtaktFattet(ident: String, vedtakFattet: VedtakObserver.VedtakFattet) {
        sikkerlogger.info { "Vedtak for $ident fattet. Vedtak: $vedtakFattet" }
        val message = JsonMessage.newMessage(
            eventName = "vedtak_fattet",
            map = mapOf(
                "ident" to ident,
                "behandlingId" to vedtakFattet.behandlingId.toString(),
                "vedtakId" to vedtakFattet.vedtakId.toString(),
                "vedtaktidspunkt" to vedtakFattet.vedtakstidspunkt,
                "virkningsdato" to vedtakFattet.virkningsdato,
                "utbetalingsdager" to vedtakFattet.utbetalingsdager,
                "utfall" to vedtakFattet.utfall.name,
            ),
        )

        rapidsConnection.publish(
            key = ident,
            message = message.toJson(),
        )
        logger.info { "Vedtak fattet melding publisert. BehandlingId: $behandlingId" }
    }
}
