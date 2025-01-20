package no.nav.dagpenger.regel

import no.nav.dagpenger.opplysning.Opplysningstype
import no.nav.dagpenger.opplysning.Opplysningstype.Companion.aldriSynlig
import no.nav.dagpenger.opplysning.Regelsett
import no.nav.dagpenger.opplysning.regel.ikke
import no.nav.dagpenger.opplysning.regel.oppslag
import no.nav.dagpenger.regel.Søknadstidspunkt.prøvingsdato

object Utestengning {
    val utestengt = Opplysningstype.somBoolsk("Bruker er utestengt fra dagpenger")
    val oppfyllerKravetTilIkkeUtestengt = Opplysningstype.somBoolsk("Oppfyller krav til ikke utestengt", synlig = aldriSynlig)

    val regelsett =
        Regelsett(
            folketrygden.hjemmel(4, 28, "Utestengning", "4-28 Utestengning"),
        ) {
            regel(utestengt) { oppslag(prøvingsdato) { false } }
            utfall(oppfyllerKravetTilIkkeUtestengt) { ikke(utestengt) }
        }
}
