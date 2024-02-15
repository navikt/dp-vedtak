package no.nav.dagpenger.behandling.modell

import no.nav.dagpenger.aktivitetslogg.Aktivitetskontekst
import no.nav.dagpenger.aktivitetslogg.SpesifikkKontekst
import no.nav.dagpenger.behandling.modell.hendelser.OpplysningSvarHendelse
import no.nav.dagpenger.behandling.modell.hendelser.PersonHendelse
import no.nav.dagpenger.behandling.modell.hendelser.SøknadInnsendtHendelse
import no.nav.dagpenger.opplysning.Opplysninger
import no.nav.dagpenger.regel.Alderskrav
import no.nav.dagpenger.regel.Minsteinntekt
import no.nav.dagpenger.regel.RettTilDagpenger
import no.nav.dagpenger.regel.Virkningsdato

class Person(
    private val ident: PersonIdentifikator,
    private val behandlinger: MutableList<Behandling> = mutableListOf(),
) : Aktivitetskontekst {
    fun ident() = ident

    fun håndter(hendelse: SøknadInnsendtHendelse) {
        hendelse.leggTilKontekst(this)
        val behandling =
            Behandling(
                hendelse,
                Opplysninger(),
                RettTilDagpenger.regelsett,
                Alderskrav.regelsett,
                Minsteinntekt.regelsett,
                Virkningsdato.regelsett,
            ).also { behandlinger.add(it) }
        behandling.håndter(hendelse)
    }

    fun håndter(hendelse: OpplysningSvarHendelse) {
        hendelse.leggTilKontekst(this)
        val behandling = behandlinger.first() // .first { it.behandlingId == hendelse.behandlingId }
        behandling.håndter(hendelse)
    }

    private fun PersonHendelse.leggTilKontekst(kontekst: Aktivitetskontekst) {
        kontekst(this)
        kontekst(kontekst)
    }

    // todo: vi trenger en metode for å hente behandlinger fra utsiden.
    fun behandlinger() = behandlinger.toList()

    override fun toSpesifikkKontekst(): SpesifikkKontekst = SpesifikkKontekst("Person", mapOf("ident" to ident.identifikator()))
}
