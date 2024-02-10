package no.nav.dagpenger.vedtak.modell

import no.nav.dagpenger.aktivitetslogg.Aktivitetskontekst
import no.nav.dagpenger.aktivitetslogg.SpesifikkKontekst
import no.nav.dagpenger.opplysning.Opplysninger
import no.nav.dagpenger.regel.Alderskrav
import no.nav.dagpenger.regel.Minsteinntekt
import no.nav.dagpenger.regel.RettTilDagpenger
import no.nav.dagpenger.regel.Virkningsdato
import no.nav.dagpenger.vedtak.modell.hendelser.SøknadInnsendtHendelse

class Person(
    private val ident: PersonIdentifikator,
) : Aktivitetskontekst {
    fun ident() = ident

    fun håndter(hendelse: SøknadInnsendtHendelse) {
        // TODO: Dette bør skje i hendelse-land
        hendelse.kontekst(hendelse)
        hendelse.kontekst(this)
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

    override fun toSpesifikkKontekst(): SpesifikkKontekst = SpesifikkKontekst("Person", mapOf("ident" to ident.identifikator()))
}
