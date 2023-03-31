package no.nav.dagpenger.vedtak.modell

import no.nav.dagpenger.vedtak.modell.hendelser.Hendelse
import no.nav.dagpenger.vedtak.modell.hendelser.Rapporteringshendelse
import no.nav.dagpenger.vedtak.modell.hendelser.SøknadBehandletHendelse
import no.nav.dagpenger.vedtak.modell.rapportering.Rapporteringsperioder
import no.nav.dagpenger.vedtak.modell.visitor.PersonVisitor

class Person(val ident: PersonIdentifikator) : Aktivitetskontekst by ident {
    private val vedtakHistorikk = VedtakHistorikk()
    private val rapporteringsperioder = Rapporteringsperioder()

    fun håndter(søknadBehandletHendelse: SøknadBehandletHendelse) {
        vedtakHistorikk.leggTilVedtak(søknadBehandletHendelse.tilVedtak())
    }

    fun håndter(rapporteringshendelse: Rapporteringshendelse) {
        kontekst(rapporteringshendelse)
        val rapporteringsperiode = rapporteringsperioder.håndter(rapporteringshendelse)
        vedtakHistorikk.håndter(rapporteringsperiode)
    }

    fun accept(visitor: PersonVisitor) {
        visitor.visitPerson(ident)
        rapporteringsperioder.accept(visitor)
        vedtakHistorikk.accept(visitor)
    }

    private fun kontekst(hendelse: Hendelse) {
        hendelse.kontekst(this)
    }

    companion object {
        val kontekstType: String = "Person"
    }
}
