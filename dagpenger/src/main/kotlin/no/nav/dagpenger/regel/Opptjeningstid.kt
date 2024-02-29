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
    private val pliktigRapporteringsfrist = Opplysningstype.somDato("Arbeidsgivers rapporteringsfrist")
    internal val justertRapporteringsfrist = Opplysningstype.somDato("Arbeidsgivers rapporteringsfrist justert for helg og helligdager")
    val sisteAvsluttendendeKalenderMåned = Opplysningstype.somDato("Siste avsluttende kalendermåned")

    val regelsett =
        Regelsett("Opptjeningsperiode") {
            regel(pliktigRapporteringsfrist) {
                oppslag(søknadstidspunkt) { søknadstidspunkt ->
                    LocalDate.of(
                        søknadstidspunkt.year,
                        søknadstidspunkt.month,
                        5,
                    )
                }
            }
            regel(justertRapporteringsfrist) { førsteArbeidsdag(pliktigRapporteringsfrist) }
            regel(sisteAvsluttendendeKalenderMåned) { sisteDagIForrigeMåned(justertRapporteringsfrist) }
        }
}
