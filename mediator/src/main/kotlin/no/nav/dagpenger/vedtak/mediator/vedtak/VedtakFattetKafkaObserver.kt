package no.nav.dagpenger.vedtak.mediator.vedtak

import mu.KotlinLogging
import mu.withLoggingContext
import no.nav.dagpenger.vedtak.modell.PersonObserver
import no.nav.dagpenger.vedtak.modell.vedtak.VedtakObserver
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.RapidsConnection

internal class VedtakFattetKafkaObserver(private val rapidsConnection: RapidsConnection) : PersonObserver {

    companion object {
        val logger = KotlinLogging.logger { }
        val sikkerlogger = KotlinLogging.logger("tjenestekall.VedtakFattetKafkaObserver")
    }

    override fun vedtakFattet(ident: String, vedtakFattet: VedtakObserver.VedtakFattet) {
        withLoggingContext(
            mapOf(
                "behandlingId" to vedtakFattet.behandlingId.toString(),
                "vedtakId" to vedtakFattet.vedtakId.toString(),
            ),
        ) {
            sikkerlogger.info { "Vedtak for $ident fattet. Vedtak: $vedtakFattet" }
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
            logger.info { "Vedtak fattet melding publisert." }
        }
    }

    override fun løpendeVedtakFattet(ident: String, løpendeVedtakFattet: VedtakObserver.LøpendeVedtakFattet) {
        withLoggingContext(
            mapOf(
                "behandlingId" to løpendeVedtakFattet.behandlingId.toString(),
                "vedtakId" to løpendeVedtakFattet.vedtakId.toString(),
            ),
        ) {
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
            logger.info { "Vedtak fattet melding publisert." }
        }
    }
}
