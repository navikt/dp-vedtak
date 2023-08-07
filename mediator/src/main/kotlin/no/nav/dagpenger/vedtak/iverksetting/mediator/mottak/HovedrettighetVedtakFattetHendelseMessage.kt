package no.nav.dagpenger.vedtak.iverksetting.mediator.mottak

import no.nav.dagpenger.vedtak.iverksetting.hendelser.HovedrettighetVedtakFattetHendelse
import no.nav.dagpenger.vedtak.mediator.IHendelseMediator
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext

internal class HovedrettighetVedtakFattetHendelseMessage(private val packet: JsonMessage) : VedtakFattetHendelseMessage(packet) {

    private val hendelse: HovedrettighetVedtakFattetHendelse get() = HovedrettighetVedtakFattetHendelse(
        meldingsreferanseId = id,
        ident = ident,
        vedtakId = vedtakId,
        behandlingId = behandlingId,
        vedtakstidspunkt = vedtakstidspunkt,
        virkningsdato = virkningsdato,
        utfall = when (packet.utfall()) {
            "Innvilget" -> HovedrettighetVedtakFattetHendelse.Utfall.Innvilget
            "Avslått" -> HovedrettighetVedtakFattetHendelse.Utfall.Avslått
            else -> throw IllegalArgumentException("Vet ikke om utfall ${packet.utfall()}")
        },
    )

    private fun JsonMessage.utfall(): String = this["utfall"].asText()
    override fun behandle(mediator: IHendelseMediator, context: MessageContext) {
        mediator.behandle(hendelse, this, context)
    }
}
