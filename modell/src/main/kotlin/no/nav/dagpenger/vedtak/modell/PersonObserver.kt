package no.nav.dagpenger.vedtak.modell

import no.nav.dagpenger.vedtak.modell.vedtak.VedtakObserver

interface PersonObserver {

    fun vedtakFattet(ident: String, vedtakFattet: VedtakObserver.VedtakFattet) {}
    fun løpendeVedtakFattet(ident: String, utbetalingVedtakFattet: VedtakObserver.UtbetalingVedtakFattet) {}
}
