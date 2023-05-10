package no.nav.dagpenger.vedtak.iverksetting.mediator.mottak

import com.fasterxml.jackson.databind.JsonNode
import no.nav.dagpenger.vedtak.iverksetting.IverksettingsVedtak
import no.nav.dagpenger.vedtak.iverksetting.hendelser.VedtakFattetHendelse
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.asLocalDate
import no.nav.helse.rapids_rivers.asLocalDateTime
import java.util.UUID

internal class VedtakFattetHendelseMessage(private val packet: JsonMessage) {

    fun hendelse(): VedtakFattetHendelse = VedtakFattetHendelse(
        ident = packet["ident"].asText(),
        iverksettingsVedtak = IverksettingsVedtak(
            vedtakId = packet["vedtakId"].asUUID(),
            behandlingId = packet["behandlingId"].asUUID(),
            vedtakstidspunkt = packet["vedtaktidspunkt"].asLocalDateTime(),
            virkningsdato = packet["virkningsdato"].asLocalDate(),
            utfall = when (packet.utfall()) {
                "Innvilget" -> IverksettingsVedtak.Utfall.Innvilget
                "Avslått" -> IverksettingsVedtak.Utfall.Avslått
                else -> {
                    throw IllegalArgumentException("Vet ikke om utfall ${packet.utfall()}")
                }
            },
        ),
    )

    private fun JsonMessage.utfall(): String = this["utfall"].asText()
}

private fun JsonNode.asUUID() = this.asText().let { UUID.fromString(it) }
