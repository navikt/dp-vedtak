package no.nav.dagpenger.vedtak.iverksetting.mediator.mottak

import no.nav.dagpenger.vedtak.mediator.melding.HendelseMessage
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.asLocalDate
import no.nav.helse.rapids_rivers.asLocalDateTime

internal abstract class VedtakFattetHendelseMessage(private val packet: JsonMessage) : HendelseMessage(packet) {

    override val ident: String
        get() = packet["ident"].asText()

    protected val vedtakId = packet["vedtakId"].asUUID()
    protected val behandlingId = packet["behandlingId"].asUUID()
    protected val sakId = packet["sakId"].asText()
    protected val vedtakstidspunkt = packet["vedtaktidspunkt"].asLocalDateTime()
    protected val virkningsdato = packet["virkningsdato"].asLocalDate()
}
