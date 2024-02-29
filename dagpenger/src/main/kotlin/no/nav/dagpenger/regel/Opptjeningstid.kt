package no.nav.dagpenger.regel

import no.nav.dagpenger.opplysning.Opplysningstype
import no.nav.dagpenger.opplysning.Regelsett
import no.nav.dagpenger.opplysning.regel.dato.førsteArbeidsdag
import no.nav.dagpenger.opplysning.regel.dato.sisteDagIForrigeMåned
import no.nav.dagpenger.opplysning.regel.oppslag
import java.time.LocalDate

/**
 * Kapittel 3A. Søknadstidspunkt, opptjeningstid mv. -
 *
 * https://lovdata.no/dokument/SF/forskrift/1998-09-16-890/KAPITTEL_4#%C2%A73a-2
 */

object Opptjeningstid {
    val søknadstidspunkt = Søknadstidspunkt.søknadstidspunkt

    // https://lovdata.no/dokument/NL/lov/2012-06-22-43/%C2%A74#%C2%A74
    private val pliktigRapporteringsfrist = Opplysningstype.somDato("Lovpålagt rapporteringsfrist for A-ordningen")
    internal val justertRapporteringsfrist = Opplysningstype.somDato("Arbeidsgivers rapporteringsfrist")
    val sisteAvsluttendendeKalenderMåned = Opplysningstype.somDato("Siste avsluttende kalendermåned")

    val regelsett =
        Regelsett("Opptjeningsperiode") {
            regel(pliktigRapporteringsfrist) { oppslag(søknadstidspunkt) { Aordningen.rapporteringsfrist(it) } }
            regel(justertRapporteringsfrist) { førsteArbeidsdag(pliktigRapporteringsfrist) }
            regel(sisteAvsluttendendeKalenderMåned) { sisteDagIForrigeMåned(justertRapporteringsfrist) }
        }
}

private object Aordningen {
    fun rapporteringsfrist(søknadstidspunkt: LocalDate): LocalDate = LocalDate.of(søknadstidspunkt.year, søknadstidspunkt.month, 5)
}
