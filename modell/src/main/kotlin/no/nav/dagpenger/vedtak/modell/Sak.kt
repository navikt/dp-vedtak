package no.nav.dagpenger.vedtak.modell

import no.nav.dagpenger.aktivitetslogg.Aktivitetskontekst
import no.nav.dagpenger.aktivitetslogg.SpesifikkKontekst
import no.nav.dagpenger.vedtak.modell.hendelser.Hendelse
import no.nav.dagpenger.vedtak.modell.hendelser.Rapporteringshendelse
import no.nav.dagpenger.vedtak.modell.hendelser.StansHendelse
import no.nav.dagpenger.vedtak.modell.hendelser.SøknadBehandletHendelse
import no.nav.dagpenger.vedtak.modell.rapportering.Behandling

typealias SakId = String

class Sak private constructor(
    private val sakId: SakId,
    private val person: Person,
    val behandlinger: MutableList<Behandling>,
) : Aktivitetskontekst {
    constructor(sakId: SakId, person: Person) : this(
        sakId = sakId,
        person = person,
        behandlinger = mutableListOf(),
    )

    override fun toSpesifikkKontekst(): SpesifikkKontekst {
        return SpesifikkKontekst("sak", mapOf("sakId" to sakId))
    }

    fun håndter(søknadBehandletHendelse: SøknadBehandletHendelse) {
        kontekst(søknadBehandletHendelse)
        if (person.vedtakHistorikk.harBehandlet(søknadBehandletHendelse.behandlingId)) {
            søknadBehandletHendelse.info("Har allerede behandlet SøknadBehandletHendelse")
            return
        }
        val vedtak = søknadBehandletHendelse.tilVedtak()
        søknadBehandletHendelse.kontekst(vedtak)
        søknadBehandletHendelse.info("Mottatt hendelse om behandlet søknad og opprettet vedtak.")
        person.leggTilVedtak(vedtak)
    }

    fun håndter(rapporteringshendelse: Rapporteringshendelse) {
        kontekst(rapporteringshendelse)
        val behandling = Behandling(person, this.sakId)
        behandlinger.add(behandling)
        behandling.håndter(rapporteringshendelse)
    }
    fun håndter(stansHendelse: StansHendelse) {
        kontekst(stansHendelse)
        if (person.vedtakHistorikk.harBehandlet(stansHendelse.behandlingId)) {
            stansHendelse.info("Har allerede behandlet StansHendelse")
            return
        }
        val vedtak = stansHendelse.tilVedtak()
        stansHendelse.kontekst(vedtak)
        stansHendelse.info("Fattet stansvedtak av Dagpenger")
        person.leggTilVedtak(vedtak)
    }

    private fun kontekst(hendelse: Hendelse) {
        hendelse.kontekst(this)
    }

    companion object {
        internal fun Collection<Sak>.finnSak(sakId: String): Sak? {
            return this.find { it.sakId == sakId }
        }
    }
}
