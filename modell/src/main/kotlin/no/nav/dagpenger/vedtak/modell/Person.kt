package no.nav.dagpenger.vedtak.modell

import no.nav.dagpenger.vedtak.modell.hendelser.NyRettighetHendelse

class Person(id: PersonIdentifikator) {

    private val vedtakHistorikk = VedtakHistorikk()

    fun håndter(nyRettighetHendelse: NyRettighetHendelse) {
        vedtakHistorikk.leggTilVedtak(nyRettighetHendelse)
    }

    fun harVedtak() = vedtakHistorikk.harVedtak()
}
