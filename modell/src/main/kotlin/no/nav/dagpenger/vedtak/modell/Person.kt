package no.nav.dagpenger.vedtak.modell

import no.nav.dagpenger.aktivitetslogg.Aktivitetskontekst
import no.nav.dagpenger.aktivitetslogg.SpesifikkKontekst
import no.nav.dagpenger.opplysning.Opplysninger
import no.nav.dagpenger.opplysning.Opplysningstype
import no.nav.dagpenger.opplysning.Regelsett
import no.nav.dagpenger.opplysning.id
import no.nav.dagpenger.opplysning.regel.alle
import no.nav.dagpenger.regel.Alderskrav
import no.nav.dagpenger.regel.Minsteinntekt
import no.nav.dagpenger.regel.Virkningsdato
import no.nav.dagpenger.vedtak.modell.hendelser.SøknadInnsendtHendelse

object RettTilDagpenger {
    val saksbehandlerSierJa = Opplysningstype<Boolean>("Saksbehandler sier ja".id("saksbehandlerSierJa"))
    val rettTilDagpenger = Opplysningstype<Boolean>("Rett til dagpenger")
    val regelsett =
        Regelsett("Krav på dagpenger").apply {
            regel(rettTilDagpenger) { alle(saksbehandlerSierJa) }
        }
}

class Person(
    private val ident: PersonIdentifikator,
) : Aktivitetskontekst {
    fun ident() = ident

    fun håndter(hendelse: SøknadInnsendtHendelse) {
        hendelse.kontekst(this)
        val krav = Opplysningstype<Boolean>("Krav på dagpenger")
        val blurp = Regelsett("Krav på dagpenger") { regel(krav) { alle(Alderskrav.vilkår, Minsteinntekt.minsteinntekt) } }
        val behandling = Behandling(hendelse, Opplysninger(), blurp, Alderskrav.regelsett, Minsteinntekt.regelsett, Virkningsdato.regelsett)
        val trenger = behandling.trenger(krav)
        trenger.map {
            hendelse.behov(
                OpplysningBehov(it.id),
                "Trenger en opplysning",
            )
        }
    }

    override fun toSpesifikkKontekst(): SpesifikkKontekst = SpesifikkKontekst("Person", mapOf("ident" to ident.identifikator()))
}
