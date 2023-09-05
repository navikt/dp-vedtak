package no.nav.dagpenger.vedtak.mediator.vedtak

import mu.KotlinLogging
import mu.withLoggingContext
import no.nav.dagpenger.vedtak.modell.PersonObserver
import no.nav.dagpenger.vedtak.modell.vedtak.VedtakObserver
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.RapidsConnection

internal class VedtakFattetObserver(private val rapidsConnection: RapidsConnection) : PersonObserver {

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
            sikkerlogger.info { "Vedtak om hovedrettighet for $ident fattet. Vedtak: $vedtakFattet" }

            val vedtakdetaljer = mapOf(
                "ident" to ident,
                "behandlingId" to vedtakFattet.behandlingId.toString(),
                "sakId" to vedtakFattet.sakId,
                "vedtakId" to vedtakFattet.vedtakId.toString(),
                "vedtaktidspunkt" to vedtakFattet.vedtakstidspunkt,
                "virkningsdato" to vedtakFattet.virkningsdato,
            )

            val eventNavn = when (vedtakFattet.utfall) {
                VedtakObserver.Utfall.Innvilget -> "dagpenger_innvilget"
                VedtakObserver.Utfall.Avslått -> "dagpenger_avslått"
            }

            val message = JsonMessage.newMessage(
                eventName = eventNavn,
                map = vedtakdetaljer,
            )

            rapidsConnection.publish(
                key = ident,
                message = message.toJson(),
            )
            logger.info { "Vedtak fattet: $eventNavn publisert." }
        }
    }

    override fun utbetalingsvedtakFattet(
        ident: String,
        utbetalingsvedtakFattet: VedtakObserver.UtbetalingsvedtakFattet,
    ) {
        withLoggingContext(
            mapOf(
                "behandlingId" to utbetalingsvedtakFattet.behandlingId.toString(),
                "vedtakId" to utbetalingsvedtakFattet.vedtakId.toString(),
            ),
        ) {
            sikkerlogger.info { "Utbetalingsvedtak for $ident fattet. Vedtak: $utbetalingsvedtakFattet" }

            val message = lagJsonMessageForFattetUtbetalingsvedtak(ident, utbetalingsvedtakFattet)

            rapidsConnection.publish(
                key = ident,
                message = message.toJson(),
            )
            logger.info { "Utbetalingsvedtak fattet publisert." }
        }
    }

    private fun lagJsonMessageForFattetUtbetalingsvedtak(
        ident: String,
        utbetalingsvedtakFattet: VedtakObserver.UtbetalingsvedtakFattet,
    ): JsonMessage {
        return JsonMessage.newMessage(
            eventName = "utbetaling_vedtak_fattet",
            map = mapOf(
                "ident" to ident,
                "behandlingId" to utbetalingsvedtakFattet.behandlingId.toString(),
                "sakId" to utbetalingsvedtakFattet.sakId,
                "vedtakId" to utbetalingsvedtakFattet.vedtakId.toString(),
                "vedtaktidspunkt" to utbetalingsvedtakFattet.vedtakstidspunkt,
                "virkningsdato" to utbetalingsvedtakFattet.virkningsdato,
                "utbetalingsdager" to utbetalingsvedtakFattet.utbetalingsdager,
                "utfall" to utbetalingsvedtakFattet.utfall.name,
            ),
        )
    }
}
