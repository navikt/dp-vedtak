package no.nav.dagpenger.vedtak.modell

import no.nav.dagpenger.aktivitetslogg.Aktivitetskontekst
import no.nav.dagpenger.aktivitetslogg.SpesifikkKontekst
import no.nav.dagpenger.vedtak.modell.hendelser.Hendelse
import no.nav.dagpenger.vedtak.modell.hendelser.RapporteringHendelse
import no.nav.dagpenger.vedtak.modell.hendelser.RettighetBehandletHendelse
import no.nav.dagpenger.vedtak.modell.hendelser.StansHendelse
import no.nav.dagpenger.vedtak.modell.rapportering.Behandling
import no.nav.dagpenger.vedtak.modell.visitor.SakVisitor

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

    init {
        person.leggTilSak(this)
    }

    override fun toSpesifikkKontekst(): SpesifikkKontekst {
        return SpesifikkKontekst("sak", mapOf("sakId" to sakId))
    }

    fun accept(sakVisitor: SakVisitor) {
        sakVisitor.visitSak(sakId)
    }

    fun håndter(rettighetBehandletHendelse: RettighetBehandletHendelse) {
        kontekst(rettighetBehandletHendelse)
        if (person.vedtakHistorikk.harBehandlet(rettighetBehandletHendelse.behandlingId)) {
            rettighetBehandletHendelse.info("Har allerede behandlet RettighetBehandletHendelse")
            return
        }
        val vedtak = rettighetBehandletHendelse.tilVedtak()
        rettighetBehandletHendelse.kontekst(vedtak)
        rettighetBehandletHendelse.info("Mottatt hendelse om behandlet søknad og opprettet vedtak.")
        person.leggTilVedtak(vedtak)
    }

    fun håndter(rapporteringHendelse: RapporteringHendelse) {
        kontekst(rapporteringHendelse)
        val behandling = Behandling(person, this.sakId)
        behandlinger.add(behandling)
        behandling.håndter(rapporteringHendelse)
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
