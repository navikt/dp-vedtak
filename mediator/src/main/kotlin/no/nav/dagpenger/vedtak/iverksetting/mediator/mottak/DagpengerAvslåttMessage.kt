package no.nav.dagpenger.vedtak.iverksetting.mediator.mottak

import no.nav.dagpenger.vedtak.iverksetting.hendelser.DagpengerAvslått
import no.nav.dagpenger.vedtak.mediator.IHendelseMediator
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext

internal class DagpengerAvslåttMessage(packet: JsonMessage) : VedtakFattetHendelseMessage(packet) {

    private val hendelse: DagpengerAvslått get() = DagpengerAvslått(
        meldingsreferanseId = id,
        ident = ident,
        vedtakId = vedtakId,
        behandlingId = behandlingId,
        vedtakstidspunkt = vedtakstidspunkt,
        virkningsdato = virkningsdato,
    )
    override fun behandle(mediator: IHendelseMediator, context: MessageContext) {
        mediator.behandle(hendelse, this, context)
    }
}
