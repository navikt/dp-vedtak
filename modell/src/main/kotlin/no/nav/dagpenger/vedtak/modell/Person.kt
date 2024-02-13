package no.nav.dagpenger.vedtak.modell

import no.nav.dagpenger.aktivitetslogg.Aktivitetskontekst
import no.nav.dagpenger.aktivitetslogg.SpesifikkKontekst
import no.nav.dagpenger.opplysning.Opplysninger
import no.nav.dagpenger.regel.Alderskrav
import no.nav.dagpenger.regel.Minsteinntekt
import no.nav.dagpenger.regel.RettTilDagpenger
import no.nav.dagpenger.regel.Virkningsdato
import no.nav.dagpenger.vedtak.modell.hendelser.PersonHendelse
import no.nav.dagpenger.vedtak.modell.hendelser.SøknadInnsendtHendelse

class Person(
    private val ident: PersonIdentifikator,
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
            )
        behandling.håndter(hendelse)
    }

    private fun PersonHendelse.leggTilKontekst(kontekst: Aktivitetskontekst) {
        kontekst(this)
        kontekst(kontekst)
    }

    override fun toSpesifikkKontekst(): SpesifikkKontekst = SpesifikkKontekst("Person", mapOf("ident" to ident.identifikator()))
}
