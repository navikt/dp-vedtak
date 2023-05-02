package no.nav.dagpenger.vedtak.mediator.vedtak.iverksett

import no.nav.dagpenger.vedtak.modell.PersonObserver
import no.nav.dagpenger.vedtak.modell.vedtak.VedtakObserver

internal class VedtakFattetIverksettObserver(iverksettClient: IverksettClient) : PersonObserver {

    override fun vedtaktFattet(ident: String, vedtakFattet: VedtakObserver.VedtakFattet) {
        TODO()
    }
}
