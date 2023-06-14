package no.nav.dagpenger.vedtak.modell

import no.nav.dagpenger.vedtak.modell.vedtak.VedtakObserver

interface PersonObserver {

    fun rammevedtakFattet(ident: String, rammevedtakFattet: VedtakObserver.RammevedtakFattet) {}
    fun løpendeVedtakFattet(ident: String, løpendeVedtakFattet: VedtakObserver.LøpendeVedtakFattet) {}
}
