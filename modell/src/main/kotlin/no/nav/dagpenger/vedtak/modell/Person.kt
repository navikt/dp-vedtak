package no.nav.dagpenger.vedtak.modell

import no.nav.dagpenger.vedtak.modell.hendelser.Hendelse
import no.nav.dagpenger.vedtak.modell.hendelser.Rapporteringshendelse
import no.nav.dagpenger.vedtak.modell.hendelser.StansHendelse
import no.nav.dagpenger.vedtak.modell.hendelser.SøknadBehandletHendelse
import no.nav.dagpenger.vedtak.modell.rapportering.Rapporteringsperioder
import no.nav.dagpenger.vedtak.modell.vedtak.VedtakHistorikk
import no.nav.dagpenger.vedtak.modell.vedtak.VedtakObserver
import no.nav.dagpenger.vedtak.modell.visitor.PersonVisitor

class Person(val ident: PersonIdentifikator) : Aktivitetskontekst by ident, VedtakObserver {
    private val vedtakHistorikk = VedtakHistorikk().also {
        it.addObserver(this)
    }

    private val observers = mutableListOf<PersonObserver>()

    private val rapporteringsperioder = Rapporteringsperioder()

    fun håndter(søknadBehandletHendelse: SøknadBehandletHendelse) {
        kontekst(søknadBehandletHendelse)
        vedtakHistorikk.håndter(søknadBehandletHendelse)
    }

    fun håndter(rapporteringshendelse: Rapporteringshendelse) {
        kontekst(rapporteringshendelse)
        val rapporteringsperiode = rapporteringsperioder.håndter(rapporteringshendelse)
        vedtakHistorikk.håndter(rapporteringsperiode)
    }

    fun håndter(stansHendelse: StansHendelse) {
        kontekst(stansHendelse)
        vedtakHistorikk.håndter(stansHendelse)
    }

    override fun vedtakFattet(vedtakFattet: VedtakObserver.VedtakFattet) {
        observers.forEach {
            it.vedtaktFattet(ident.identifikator(), vedtakFattet)
        }
    }

    fun addObserver(personObserver: PersonObserver) {
        observers.add(personObserver)
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
