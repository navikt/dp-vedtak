package no.nav.dagpenger.vedtak.modell

import no.nav.dagpenger.aktivitetslogg.Aktivitetskontekst
import no.nav.dagpenger.aktivitetslogg.SpesifikkKontekst
import no.nav.dagpenger.opplysning.Opplysninger
import no.nav.dagpenger.opplysning.Opplysningstype
import no.nav.dagpenger.opplysning.Regelsett
import no.nav.dagpenger.opplysning.regel.alle
import no.nav.dagpenger.vedtak.modell.hendelser.SøknadInnsendtHendelse

object RettTilDagpenger {
    val saksbehandlerSierJa = Opplysningstype<Boolean>("Saksbehandler sier ja")
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
        val behandling = Behandling(hendelse, Opplysninger(), RettTilDagpenger.regelsett)
        val trenger = behandling.trenger()
        trenger.map {
            hendelse.behov(
                OpplysningBehov(it.navn),
                "Trenger en opplysning",
            )
        }
    }

    override fun toSpesifikkKontekst(): SpesifikkKontekst = SpesifikkKontekst("Person", mapOf("ident" to ident.identifikator()))
}
