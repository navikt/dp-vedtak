package no.nav.dagpenger.behandling.modell

import no.nav.dagpenger.aktivitetslogg.Aktivitetskontekst
import no.nav.dagpenger.aktivitetslogg.SpesifikkKontekst
import no.nav.dagpenger.behandling.modell.Behandling.Companion.finn
import no.nav.dagpenger.behandling.modell.hendelser.AvbrytBehandlingHendelse
import no.nav.dagpenger.behandling.modell.hendelser.OpplysningSvarHendelse
import no.nav.dagpenger.behandling.modell.hendelser.PersonHendelse
import no.nav.dagpenger.behandling.modell.hendelser.SøknadInnsendtHendelse

class Person(
    val ident: Ident,
    behandlinger: List<Behandling>,
) : Aktivitetskontekst {
    private val behandlinger = behandlinger.toMutableList()

    constructor(ident: Ident) : this(ident, mutableListOf())

    fun håndter(hendelse: SøknadInnsendtHendelse) {
        hendelse.leggTilKontekst(this)
        val behandling =
            Behandling(
                hendelse,
                emptyList(),
            ).also { behandling ->
                behandlinger.add(behandling)
            }
        behandling.håndter(hendelse)
    }

    fun håndter(hendelse: OpplysningSvarHendelse) {
        hendelse.leggTilKontekst(this)
        val behandling =
            try {
                behandlinger.finn(hendelse.behandlingId)
            } catch (e: NoSuchElementException) {
                // TODO: Behandlingen mangler - hopp til neste melding - det må vi slutte med
                return
            }
        behandling.håndter(hendelse)
    }

    fun håndter(hendelse: AvbrytBehandlingHendelse) {
        hendelse.leggTilKontekst(this)
        val behandling =
            try {
                behandlinger.finn(hendelse.behandlingId)
            } catch (e: NoSuchElementException) {
                // TODO: Behandlingen mangler - hopp til neste melding - det må vi slutte med
                return
            }
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
