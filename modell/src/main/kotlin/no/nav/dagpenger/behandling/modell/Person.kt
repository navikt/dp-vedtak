package no.nav.dagpenger.behandling.modell

import no.nav.dagpenger.aktivitetslogg.Aktivitetskontekst
import no.nav.dagpenger.aktivitetslogg.SpesifikkKontekst
import no.nav.dagpenger.behandling.modell.Behandling.Companion.finn
import no.nav.dagpenger.behandling.modell.BehandlingObservatør.BehandlingEndretTilstand
import no.nav.dagpenger.behandling.modell.hendelser.AvbrytBehandlingHendelse
import no.nav.dagpenger.behandling.modell.hendelser.AvklaringIkkeRelevantHendelse
import no.nav.dagpenger.behandling.modell.hendelser.ForslagGodkjentHendelse
import no.nav.dagpenger.behandling.modell.hendelser.MeldekortBeregningHendelse
import no.nav.dagpenger.behandling.modell.hendelser.MeldekortMottattHendelse
import no.nav.dagpenger.behandling.modell.hendelser.OpplysningSvarHendelse
import no.nav.dagpenger.behandling.modell.hendelser.PersonHendelse
import no.nav.dagpenger.behandling.modell.hendelser.PåminnelseHendelse
import no.nav.dagpenger.behandling.modell.hendelser.SøknadInnsendtHendelse
import no.nav.dagpenger.opplysning.Opplysninger
import no.nav.dagpenger.regel.beregning.Beregning.arbeidsdag
import no.nav.dagpenger.regel.beregning.Beregning.meldeperiodeBehandlet
import java.util.UUID

class Meldekort(
    val meldekortId: UUID,
    val opplysninger: Opplysninger,
) {
    private val meldekortperiode = opplysninger.finnOpplysning(meldeperiodeBehandlet)
    val periode = meldekortperiode.gyldighetsperiode
    val fraOgMed = periode.fom
    val tilOgMed = periode.tom
    val erBehandlet = meldekortperiode.verdi
    val arbeidsdager = opplysninger.finnAlle().filter { it.opplysningstype == arbeidsdag }
}

class Person(
    val ident: Ident,
    behandlinger: List<Behandling>,
) : Aktivitetskontekst,
    PersonHåndter {
    private val observatører = mutableSetOf<PersonObservatør>()
    private val behandlinger = behandlinger.toMutableList()
    val meldekort = mutableListOf<Meldekort>()

    constructor(ident: Ident) : this(ident, mutableListOf())

    override fun håndter(hendelse: SøknadInnsendtHendelse) {
        /*
        TODO: Behandlinger bør spørres om de skal håndtere hendelsen
        if (behandlinger.any { it.behandler.eksternId == hendelse.eksternId }) {
            hendelse.varsel("Søknad med eksternId ${hendelse.eksternId} er allerede mottatt")
            return
        }*/
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

    override fun håndter(hendelse: MeldekortMottattHendelse) {
        hendelse.leggTilKontekst(this)
        val meldekort = hendelse.somMeldekort()
        this.meldekort.add(meldekort)
    }

    /*override fun håndter(hendelse: BeregningsperiodeHendelse) {
        hendelse.leggTilKontekst(this)
        val meldekortTilBehandling = meldekort.first { !it.erBehandlet }

        // TODO: Skal meldekortet behandles?
        val forrigeBehandling = behandlinger.last()
        val behandling =
            Behandling(
                behandler = hendelse,
                opplysninger = meldekortTilBehandling.opplysninger.finnAlle(), // TODO: Skal vi gjøre dette?
                basertPå = listOf(forrigeBehandling),
            )
    }*/

    override fun håndter(hendelse: AvklaringIkkeRelevantHendelse) {
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

    override fun håndter(hendelse: PåminnelseHendelse) {
        hendelse.leggTilKontekst(this)
        val behandling = behandlinger.finn(hendelse.behandlingId)
        behandling.håndter(hendelse)
    }

    override fun håndter(hendelse: MeldekortBeregningHendelse) {
        TODO("Not yet implemented")
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
        override fun endretTilstand(event: BehandlingEndretTilstand) {
            delegate.endretTilstand(event)
            delegate.endretTilstand(PersonObservatør.PersonEvent(ident, event))
        }
    }
}

interface PersonObservatør : BehandlingObservatør {
    fun endretTilstand(event: PersonEvent<BehandlingEndretTilstand>) {}

    data class PersonEvent<T>(
        val ident: String,
        val wrappedEvent: T,
    )
}
