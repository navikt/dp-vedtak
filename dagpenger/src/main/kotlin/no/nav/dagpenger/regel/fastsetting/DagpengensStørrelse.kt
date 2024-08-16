package no.nav.dagpenger.regel.fastsetting

import no.nav.dagpenger.opplysning.Opplysningstype
import no.nav.dagpenger.opplysning.Regelsett
import no.nav.dagpenger.opplysning.regel.avrund
import no.nav.dagpenger.opplysning.regel.multiplikasjon
import no.nav.dagpenger.opplysning.regel.oppslag
import no.nav.dagpenger.regel.Søknadstidspunkt

object DagpengensStørrelse {
    private val grunnlag = Dagpengegrunnlag.grunnlag
    private val barn = Opplysningstype.somHeltall("Antall barn")
    private val faktor = Opplysningstype.somDesimaltall("Faktor")
    val dagpengensStørrelse = Opplysningstype.somBeløp("Dagpengens størrelse")
    val dagsatsUtenbarn = Opplysningstype.somBeløp("Dagsats uten barn")

    val regelsett =
        Regelsett("§ 4-12. Dagpengens størrelse (Sats)") {
            regel(faktor) {
                oppslag(Søknadstidspunkt.søknadstidspunkt) { 0.0024 }
            } // 2,4% av grunnlag. TODO: Hent faktor fra konfiguasjon som er datostyrt
            regel(dagpengensStørrelse) { multiplikasjon(grunnlag, faktor) }
            regel(dagsatsUtenbarn) { avrund(dagpengensStørrelse) }
        }
}
