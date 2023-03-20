package no.nav.dagpenger.vedtak.modell

import no.nav.dagpenger.vedtak.modell.hendelser.SøknadBehandletHendelse
import no.nav.dagpenger.vedtak.modell.visitor.PersonVisitor

class Person(private val ident: PersonIdentifikator) {
    private val vedtakHistorikk = VedtakHistorikk()

    fun håndter(søknadBehandletHendelse: SøknadBehandletHendelse) {
        vedtakHistorikk.leggTilVedtak(søknadBehandletHendelse.tilVedtak())
    }

    fun accept(visitor: PersonVisitor) {
        visitor.visitPerson(ident)
        vedtakHistorikk.accept(visitor)
    }
}
