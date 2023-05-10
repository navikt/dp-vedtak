package no.nav.dagpenger.vedtak.iverksetting.mediator.mottak

import com.fasterxml.jackson.databind.JsonNode
import no.nav.dagpenger.vedtak.iverksetting.IverksettingsVedtak
import no.nav.dagpenger.vedtak.iverksetting.hendelser.VedtakFattetHendelse
import no.nav.dagpenger.vedtak.mediator.persistens.HendelseMessage
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.asLocalDate
import no.nav.helse.rapids_rivers.asLocalDateTime
import java.util.UUID

internal class VedtakFattetHendelseMessage(private val packet: JsonMessage) : HendelseMessage {

    private val ident = packet["ident"].asText()
    private val vedtakId = packet["vedtakId"].asUUID()

    fun hendelse(): VedtakFattetHendelse {
        return VedtakFattetHendelse(
            ident = ident,
            iverksettingsVedtak = IverksettingsVedtak(
                vedtakId = vedtakId,
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
    }

    private fun JsonMessage.utfall(): String = this["utfall"].asText()

    override fun asJson(): String {
        TODO("Not yet implemented")
    }

    override fun eier(): String = ident

    override fun meldingId() = vedtakId.toString()
}

private fun JsonNode.asUUID() = this.asText().let { UUID.fromString(it) }
