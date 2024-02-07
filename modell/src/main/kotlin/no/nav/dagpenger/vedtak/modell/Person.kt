package no.nav.dagpenger.vedtak.modell

import no.nav.dagpenger.aktivitetslogg.Aktivitetskontekst
import no.nav.dagpenger.aktivitetslogg.SpesifikkKontekst
import no.nav.dagpenger.behandling.Opplysninger
import no.nav.dagpenger.behandling.Opplysningstype
import no.nav.dagpenger.behandling.Regelsett
import no.nav.dagpenger.behandling.regel.alle
import no.nav.dagpenger.vedtak.modell.hendelser.Hendelse
import no.nav.dagpenger.vedtak.modell.hendelser.SøknadInnsendtHendelse

object RettTilDagpenger {
    val saksbehandlerSierJa = Opplysningstype<Boolean>("Saksbehandler sier ja")
    val rettTilDagpenger = Opplysningstype<Boolean>("Rett til dagpenger")
    val regelsett =
        Regelsett("Krav på dagpenger").apply {
            alle(rettTilDagpenger, saksbehandlerSierJa)
        }
}

class Person(
    private val ident: PersonIdentifikator,
) : Aktivitetskontekst {
    fun ident() = ident

    fun håndter(hendelse: SøknadInnsendtHendelse) {
        kontekst(hendelse)
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

    private fun kontekst(hendelse: Hendelse) {
        hendelse.kontekst(this)
    }
}
