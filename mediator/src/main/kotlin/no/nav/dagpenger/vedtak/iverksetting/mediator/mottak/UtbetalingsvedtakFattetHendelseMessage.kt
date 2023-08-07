package no.nav.dagpenger.vedtak.iverksetting.mediator.mottak

import no.nav.dagpenger.vedtak.iverksetting.hendelser.UtbetalingsvedtakFattetHendelse
import no.nav.dagpenger.vedtak.mediator.IHendelseMediator
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.asLocalDate

internal class UtbetalingsvedtakFattetHendelseMessage(private val packet: JsonMessage) :
    VedtakFattetHendelseMessage(packet) {

    private val hendelse: UtbetalingsvedtakFattetHendelse
        get() = UtbetalingsvedtakFattetHendelse(
            meldingsreferanseId = id,
            ident = ident,
            vedtakId = vedtakId,
            behandlingId = behandlingId,
            vedtakstidspunkt = vedtakstidspunkt,
            virkningsdato = virkningsdato,
            utbetalingsdager = utbetalingsdager(),
            utfall = when (packet.utfall()) {
                "Innvilget" -> UtbetalingsvedtakFattetHendelse.Utfall.Innvilget
                "Avslått" -> UtbetalingsvedtakFattetHendelse.Utfall.Avslått
                else -> throw IllegalArgumentException("Vet ikke om utfall ${packet.utfall()}")
            },

        )

    private fun JsonMessage.utfall(): String = this["utfall"].asText()
    private fun utbetalingsdager() = packet["utbetalingsdager"].map { løpendeRettighetsdagJson ->
        UtbetalingsvedtakFattetHendelse.Utbetalingsdag(
            dato = løpendeRettighetsdagJson["dato"].asLocalDate(),
            beløp = løpendeRettighetsdagJson["beløp"].asDouble(),
        )
    }.toList()

    override fun behandle(mediator: IHendelseMediator, context: MessageContext) {
        mediator.behandle(hendelse, this, context)
    }
}
