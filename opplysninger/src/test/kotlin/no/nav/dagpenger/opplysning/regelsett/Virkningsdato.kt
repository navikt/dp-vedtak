package no.nav.dagpenger.opplysning.regelsett

import no.nav.dagpenger.opplysning.Opplysningstype
import no.nav.dagpenger.opplysning.Regelsett
import no.nav.dagpenger.opplysning.regel.dato.sisteAv
import no.nav.dagpenger.opplysning.regel.innhentes

internal object Virkningsdato {
    val søknadsdato = Opplysningstype.somDato("Søknadsdato")
    val sisteDagMedArbeidsplikt = Opplysningstype.somDato("Siste dag med arbeidsplikt")
    val sisteDagMedLønn = Opplysningstype.somDato("Siste dag med lønn")

    val virkningsdato = Opplysningstype.somDato("Virkningsdato")
    val regelsett =
        Regelsett("alder").apply {
            regel(søknadsdato) { innhentes }
            regel(sisteDagMedArbeidsplikt) { innhentes }
            regel(sisteDagMedLønn) { innhentes }
            regel(virkningsdato) { sisteAv(søknadsdato, sisteDagMedArbeidsplikt, sisteDagMedLønn) }
        }
}
