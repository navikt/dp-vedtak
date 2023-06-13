package no.nav.dagpenger.vedtak.iverksetting.mediator.mottak

import no.nav.dagpenger.vedtak.iverksetting.IverksettingsVedtak
import no.nav.dagpenger.vedtak.iverksetting.hendelser.VedtakFattetHendelse
import no.nav.dagpenger.vedtak.mediator.IHendelseMediator
import no.nav.dagpenger.vedtak.mediator.melding.HendelseMessage
import no.nav.dagpenger.vedtak.modell.entitet.Beløp.Companion.beløp
import no.nav.dagpenger.vedtak.modell.utbetaling.BeregnetBeløpDag
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext
import no.nav.helse.rapids_rivers.asLocalDate
import no.nav.helse.rapids_rivers.asLocalDateTime

internal class VedtakFattetHendelseMessage(private val packet: JsonMessage) : HendelseMessage(packet) {

    override val ident: String
        get() = packet["ident"].asText()

    private val vedtakId = packet["vedtakId"].asUUID()

    private val hendelse
        get() = VedtakFattetHendelse(
            ident = ident,
            iverksettingsVedtak = IverksettingsVedtak(
                vedtakId = vedtakId,
                behandlingId = packet["behandlingId"].asUUID(),
                vedtakstidspunkt = packet["vedtaktidspunkt"].asLocalDateTime(),
                virkningsdato = packet["virkningsdato"].asLocalDate(),
                utbetalingsdager = utbetalingsdager(), // TODO: Map om fra intern modell (BeregnetBeløpDag) til f.eks. Utbetalingsdag og fjern dager med nullbeløp
                utfall = when (packet.utfall()) {
                    "Innvilget" -> IverksettingsVedtak.Utfall.Innvilget
                    "Avslått" -> IverksettingsVedtak.Utfall.Avslått
                    else -> {
                        throw IllegalArgumentException("Vet ikke om utfall ${packet.utfall()}")
                    }
                },
            ),
        )

    override fun behandle(mediator: IHendelseMediator, context: MessageContext) {
        mediator.behandle(hendelse, this, context)
    }

    private fun utbetalingsdager() =
        packet["utbetalingsdager"].map { løpendeRettighetsdagJson ->
            BeregnetBeløpDag(
                dato = løpendeRettighetsdagJson["dato"].asLocalDate(),
                beløp = løpendeRettighetsdagJson["beløp"].asDouble().beløp,
            )
        }.toList()

    private fun JsonMessage.utfall(): String = this["utfall"].asText()
}
