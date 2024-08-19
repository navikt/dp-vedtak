package no.nav.dagpenger.regel.fastsetting

import no.nav.dagpenger.opplysning.Opplysningstype
import no.nav.dagpenger.opplysning.Regelsett
import no.nav.dagpenger.opplysning.regel.multiplikasjon
import no.nav.dagpenger.opplysning.regel.oppslag
import no.nav.dagpenger.regel.Søknadstidspunkt

object Egenandel {
    val egenandel = Opplysningstype.somBeløp("Egenandel")
    private val sats = DagpengensStørrelse.avrundetDagsMedBarnetillegg
    private val faktor = Opplysningstype.somDesimaltall("Antall dagsats for egenandel")

    val regelsett =
        Regelsett("§ 4-9. Egenandel") {
            regel(faktor) { oppslag(Søknadstidspunkt.søknadstidspunkt) { 3.0 } }
            regel(egenandel) { multiplikasjon(sats, faktor) }
        }
}
