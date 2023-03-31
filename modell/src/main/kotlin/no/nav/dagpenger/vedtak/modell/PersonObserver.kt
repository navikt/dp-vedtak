package no.nav.dagpenger.vedtak.modell

import no.nav.dagpenger.vedtak.modell.vedtak.VedtakObserver

interface PersonObserver {

    fun vedtaktFattet(ident: String, vedtakFattet: VedtakObserver.VedtakFattet) {}
}
