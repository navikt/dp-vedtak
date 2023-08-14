package no.nav.dagpenger.vedtak.iverksetting.mediator.mottak

import no.nav.dagpenger.vedtak.iverksetting.hendelser.DagpengerInnvilget
import no.nav.dagpenger.vedtak.mediator.IHendelseMediator
import no.nav.helse.rapids_rivers.JsonMessage
import no.nav.helse.rapids_rivers.MessageContext

internal class DagpengerInnvilgetMessage(packet: JsonMessage) : VedtakFattetHendelseMessage(packet) {

    private val hendelse: DagpengerInnvilget get() = DagpengerInnvilget(
        meldingsreferanseId = id,
        ident = ident,
        vedtakId = vedtakId,
        behandlingId = behandlingId,
        sakId = sakId,
        vedtakstidspunkt = vedtakstidspunkt,
        virkningsdato = virkningsdato,
    )
    override fun behandle(mediator: IHendelseMediator, context: MessageContext) {
        mediator.behandle(hendelse, this, context)
    }
}
