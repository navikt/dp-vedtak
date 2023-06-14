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

    override fun rammevedtakFattet(ident: String, rammevedtakFattet: VedtakObserver.RammevedtakFattet) {
        sikkerlogger.info { "Vedtak for $ident fattet. Vedtak: $rammevedtakFattet" }
        val message = JsonMessage.newMessage(
            eventName = "vedtak_fattet",
            map = mapOf(
                "ident" to ident,
                "behandlingId" to rammevedtakFattet.behandlingId.toString(),
                "vedtakId" to rammevedtakFattet.vedtakId.toString(),
                "vedtaktidspunkt" to rammevedtakFattet.vedtakstidspunkt,
                "virkningsdato" to rammevedtakFattet.virkningsdato,
                "utfall" to rammevedtakFattet.utfall.name,
            ),
        )

        rapidsConnection.publish(
            key = ident,
            message = message.toJson(),
        )
        logger.info { "Vedtak fattet melding publisert. BehandlingId: $behandlingId" }
    }

    override fun løpendeVedtakFattet(ident: String, løpendeVedtakFattet: VedtakObserver.LøpendeVedtakFattet) {
        sikkerlogger.info { "Vedtak for $ident fattet. Vedtak: $løpendeVedtakFattet" }
        val message = JsonMessage.newMessage(
            eventName = "vedtak_fattet",
            map = mapOf(
                "ident" to ident,
                "behandlingId" to løpendeVedtakFattet.behandlingId.toString(),
                "vedtakId" to løpendeVedtakFattet.vedtakId.toString(),
                "vedtaktidspunkt" to løpendeVedtakFattet.vedtakstidspunkt,
                "virkningsdato" to løpendeVedtakFattet.virkningsdato,
                "utbetalingsdager" to løpendeVedtakFattet.utbetalingsdager,
                "utfall" to løpendeVedtakFattet.utfall.name,
            ),
        )

        rapidsConnection.publish(
            key = ident,
            message = message.toJson(),
        )
        logger.info { "Vedtak fattet melding publisert. BehandlingId: $behandlingId" }
    }
}
