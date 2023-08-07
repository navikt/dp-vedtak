package no.nav.dagpenger.vedtak.iverksetting.mediator.mottak

import no.nav.dagpenger.vedtak.iverksetting.IverksettingsVedtak.Utbetalingsdag
import no.nav.dagpenger.vedtak.mediator.IHendelseMediator
import no.nav.dagpenger.vedtak.mediator.melding.HendelseMessage
import no.nav.dagpenger.vedtak.modell.hendelser.Hendelse
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.asLocalDate

internal class VedtakFattetHendelseMessage(private val packet: JsonMessage) : HendelseMessage(packet) {

    override val ident: String
        get() = packet["ident"].asText()

    private val vedtakId = packet["vedtakId"].asUUID()

    private val hendelse: Hendelse
        get() = TODO()
            /*VedtakFattetHendelse(
            meldingsreferanseId = id,
            ident = ident,
            iverksettingsVedtak = IverksettingsVedtak(
                vedtakId = vedtakId,
                behandlingId = packet["behandlingId"].asUUID(),
                vedtakstidspunkt = packet["vedtaktidspunkt"].asLocalDateTime(),
                virkningsdato = packet["virkningsdato"].asLocalDate(),
                utbetalingsdager = utbetalingsdager(),
                utfall = when (packet.utfall()) {
                    "Innvilget" -> Innvilget
                    "Avslått" -> Avslått
                    else -> throw IllegalArgumentException("Vet ikke om utfall ${packet.utfall()}")
                },
            ),
        ) */

    override fun behandle(mediator: IHendelseMediator, context: MessageContext) {
        TODO()
        // mediator.behandle(hendelse, this, context)
    }

    private fun utbetalingsdager() = packet["utbetalingsdager"].map { løpendeRettighetsdagJson ->
        Utbetalingsdag(
            dato = løpendeRettighetsdagJson["dato"].asLocalDate(),
            beløp = løpendeRettighetsdagJson["beløp"].asDouble(),
        )
    }.toList()

    private fun JsonMessage.utfall(): String = this["utfall"].asText()
}
