package no.nav.dagpenger.behandling.modell

import no.nav.dagpenger.aktivitetslogg.Aktivitetskontekst
import no.nav.dagpenger.aktivitetslogg.SpesifikkKontekst
import no.nav.dagpenger.behandling.modell.Behandling.Companion.finn
import no.nav.dagpenger.behandling.modell.hendelser.AvbrytBehandlingHendelse
import no.nav.dagpenger.behandling.modell.hendelser.ForslagGodkjentHendelse
import no.nav.dagpenger.behandling.modell.hendelser.ManuellBehandlingAvklartHendelse
import no.nav.dagpenger.behandling.modell.hendelser.OpplysningSvarHendelse
import no.nav.dagpenger.behandling.modell.hendelser.PersonHendelse
import no.nav.dagpenger.behandling.modell.hendelser.SøknadInnsendtHendelse
import no.nav.dagpenger.opplysning.Faktum
import no.nav.dagpenger.opplysning.Opplysningstype

class Person(
    val ident: Ident,
    behandlinger: List<Behandling>,
) : Aktivitetskontekst, PersonHåndter {
    private val behandlinger = behandlinger.toMutableList()

    constructor(ident: Ident) : this(ident, mutableListOf())

    override fun håndter(hendelse: SøknadInnsendtHendelse) {
        hendelse.leggTilKontekst(this)
        val behandling =
            Behandling(
                hendelse,
                listOf(Faktum(Opplysningstype.somHeltall("fagsakId"), hendelse.fagsakId)),
            ).also { behandling ->
                behandlinger.add(behandling)
            }
        behandling.håndter(hendelse)
    }

    override fun håndter(hendelse: ManuellBehandlingAvklartHendelse) {
        hendelse.leggTilKontekst(this)
        val behandling = behandlinger.finn(hendelse.behandlingId)
        behandling.håndter(hendelse)
    }

    override fun håndter(hendelse: OpplysningSvarHendelse) {
        hendelse.leggTilKontekst(this)
        val behandling = behandlinger.finn(hendelse.behandlingId)
        behandling.håndter(hendelse)
    }

    override fun håndter(hendelse: AvbrytBehandlingHendelse) {
        hendelse.leggTilKontekst(this)
        val behandling = behandlinger.finn(hendelse.behandlingId)
        behandling.håndter(hendelse)
    }

    override fun håndter(hendelse: ForslagGodkjentHendelse) {
        hendelse.leggTilKontekst(this)
        val behandling = behandlinger.finn(hendelse.behandlingId)
        behandling.håndter(hendelse)
    }

    fun behandlinger() = behandlinger.toList()

    private fun PersonHendelse.leggTilKontekst(kontekst: Aktivitetskontekst) {
        kontekst(this)
        kontekst(kontekst)
    }

    override fun toSpesifikkKontekst(): SpesifikkKontekst = PersonKontekst(ident.identifikator())

    data class PersonKontekst(val ident: String) : SpesifikkKontekst("Person") {
        override val kontekstMap = mapOf("ident" to ident)
    }
}
