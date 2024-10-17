package no.nav.dagpenger.behandling.modell

import no.nav.dagpenger.aktivitetslogg.Aktivitetskontekst
import no.nav.dagpenger.aktivitetslogg.SpesifikkKontekst
import no.nav.dagpenger.behandling.modell.Behandling.Companion.finn
import no.nav.dagpenger.behandling.modell.BehandlingObservatør.BehandlingEndretTilstand
import no.nav.dagpenger.behandling.modell.BehandlingObservatør.BehandlingFerdig
import no.nav.dagpenger.behandling.modell.PersonObservatør.PersonEvent
import no.nav.dagpenger.behandling.modell.hendelser.AvbrytBehandlingHendelse
import no.nav.dagpenger.behandling.modell.hendelser.AvklaringIkkeRelevantHendelse
import no.nav.dagpenger.behandling.modell.hendelser.ForslagGodkjentHendelse
import no.nav.dagpenger.behandling.modell.hendelser.NyOpplysningHendelse
import no.nav.dagpenger.behandling.modell.hendelser.PersonHendelse
import no.nav.dagpenger.behandling.modell.hendelser.PåminnelseHendelse
import no.nav.dagpenger.behandling.modell.hendelser.SøknadInnsendtHendelse

class Person(
    val ident: Ident,
    behandlinger: List<Behandling>,
) : Aktivitetskontekst,
    PersonHåndter {
    private val observatører = mutableSetOf<PersonObservatør>()
    private val behandlinger = behandlinger.toMutableList()

    constructor(ident: Ident) : this(ident, mutableListOf())

    override fun håndter(hendelse: SøknadInnsendtHendelse) {
        if (behandlinger.any { it.behandler.eksternId == hendelse.eksternId }) {
            hendelse.varsel("Søknad med eksternId ${hendelse.eksternId} er allerede mottatt")
            return
        }
        hendelse.leggTilKontekst(this)
        val behandling =
            hendelse.behandling().also { behandling ->
                behandlinger.add(behandling)
                observatører.forEach {
                    behandling.registrer(
                        PersonObservatørAdapter(ident.identifikator(), it),
                    )
                }
            }
        behandling.håndter(hendelse)
    }

    override fun håndter(hendelse: AvklaringIkkeRelevantHendelse) {
        hendelse.leggTilKontekst(this)
        val behandling = behandlinger.finn(hendelse.behandlingId)
        behandling.håndter(hendelse)
    }

    override fun håndter(hendelse: NyOpplysningHendelse) {
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

    override fun håndter(hendelse: PåminnelseHendelse) {
        hendelse.leggTilKontekst(this)
        val behandling = behandlinger.finn(hendelse.behandlingId)
        behandling.håndter(hendelse)
    }

    fun behandlinger() = behandlinger.toList()

    fun registrer(observatør: PersonObservatør) {
        observatører.add(observatør)
        behandlinger.forEach { it.registrer(PersonObservatørAdapter(ident.identifikator(), observatør)) }
    }

    private fun PersonHendelse.leggTilKontekst(kontekst: Aktivitetskontekst) {
        kontekst(this)
        kontekst(kontekst)
    }

    override fun toSpesifikkKontekst(): SpesifikkKontekst = PersonKontekst(ident.identifikator())

    data class PersonKontekst(
        val ident: String,
    ) : SpesifikkKontekst("Person") {
        override val kontekstMap = mapOf("ident" to ident)
    }

    private class PersonObservatørAdapter(
        private val ident: String,
        private val delegate: PersonObservatør,
    ) : PersonObservatør {
        override fun ferdig(event: BehandlingFerdig) {
            event.medIdent { delegate.ferdig(it) }
        }

        override fun endretTilstand(event: BehandlingEndretTilstand) {
            event.medIdent { delegate.endretTilstand(it) }
        }

        private fun <T : PersonEvent> T.medIdent(block: (T) -> Unit) = block(this.also { it.ident = this@PersonObservatørAdapter.ident })
    }
}

interface PersonObservatør : BehandlingObservatør {
    sealed class PersonEvent(
        var ident: String? = null,
    )
}
