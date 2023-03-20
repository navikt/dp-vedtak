package no.nav.dagpenger.vedtak.modell

import no.nav.dagpenger.vedtak.modell.hendelser.SøknadBehandletHendelse

class Person(private val id: PersonIdentifikator) {
    private val vedtakHistorikk = VedtakHistorikk()

    fun håndter(søknadBehandletHendelse: SøknadBehandletHendelse) {
        vedtakHistorikk.leggTilVedtak(søknadBehandletHendelse.tilVedtak())
    }
}
