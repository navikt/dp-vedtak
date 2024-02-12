package no.nav.dagpenger.opplysning.regelsett

import no.nav.dagpenger.opplysning.Opplysningstype
import no.nav.dagpenger.opplysning.Regelsett
import no.nav.dagpenger.opplysning.regel.dato.sisteAv
import no.nav.dagpenger.opplysning.regel.ekstern
import no.nav.dagpenger.opplysning.regelsett.Alderskrav.fødselsdato
import java.time.LocalDate

internal object Virkningsdato {
    // val fødselsdato = Opplysningstype<LocalDate>("Fødselsdato")
    val søknadsdato = Opplysningstype<LocalDate>("Søknadsdato")
    val sisteDagMedArbeidsplikt = Opplysningstype<LocalDate>("Siste dag med arbeidsplikt")
    val sisteDagMedLønn = Opplysningstype<LocalDate>("Siste dag med lønn")

    val virkningsdato = Opplysningstype<LocalDate>("Virkningsdato")
    val regelsett =
        Regelsett("alder").apply {
            regel(søknadsdato) { ekstern() }
            regel(sisteDagMedArbeidsplikt) { ekstern() }
            regel(sisteDagMedLønn) { ekstern() }
            regel(virkningsdato) { sisteAv(fødselsdato, søknadsdato, sisteDagMedArbeidsplikt, sisteDagMedLønn) }
        }
}
