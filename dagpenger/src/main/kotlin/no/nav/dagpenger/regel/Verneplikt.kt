package no.nav.dagpenger.regel

import no.nav.dagpenger.avklaring.Kontrollpunkt
import no.nav.dagpenger.opplysning.Opplysningstype
import no.nav.dagpenger.opplysning.Regelsett
import no.nav.dagpenger.opplysning.id
import no.nav.dagpenger.opplysning.regel.erSann
import no.nav.dagpenger.opplysning.regel.innhentMed
import no.nav.dagpenger.regel.KravPåDagpenger.kravPåDagpenger
import no.nav.dagpenger.regel.Søknadstidspunkt.søknadIdOpplysningstype

object Verneplikt {
    val avtjentVerneplikt = Opplysningstype.somBoolsk("Avtjent verneplikt".id("Verneplikt"))
    val oppfyllerKravetTilVerneplikt =
        Opplysningstype.somBoolsk("Har utført minst tre måneders militærtjeneste eller obligatorisk sivilforsvarstjeneste")

    val regelsett =
        Regelsett(
            folketrygden.hjemmel(4, 19, "Dagpenger etter avtjent verneplikt", "4-19 Verneplikt"),
        ) {
            regel(avtjentVerneplikt) { innhentMed(søknadIdOpplysningstype) }
            utfall(oppfyllerKravetTilVerneplikt) { erSann(avtjentVerneplikt) }

            avklaring(Avklaringspunkter.Verneplikt)

            relevantHvis {
                val harKrav = it.har(kravPåDagpenger) && it.finnOpplysning(kravPåDagpenger).verdi

                if (harKrav) {
                    it.finnOpplysning(avtjentVerneplikt).verdi == false
                } else {
                    true
                }
            }
        }

    val VernepliktKontroll =
        Kontrollpunkt(sjekker = Avklaringspunkter.Verneplikt) { opplysninger ->
            opplysninger.har(avtjentVerneplikt) && opplysninger.finnOpplysning(avtjentVerneplikt).verdi
        }
}
