package no.nav.dagpenger.vedtak.modell.regelsett

import no.nav.dagpenger.opplysning.Opplysningstype
import no.nav.dagpenger.opplysning.Regelsett
import no.nav.dagpenger.opplysning.regel.dato.sisteAv
import java.time.LocalDate

object Virkningsdato {
    val fødselsdato = Opplysningstype<LocalDate>("Fødselsdato")
    val søknadsdato = Opplysningstype<LocalDate>("Søknadsdato")
    val sisteDagMedArbeidsplikt = Opplysningstype<LocalDate>("Siste dag med arbeidsplikt")
    val sisteDagMedLønn = Opplysningstype<LocalDate>("Siste dag med lønn")

    val virkningsdato = Opplysningstype<LocalDate>("Virkningsdato")
    val regelsett =
        Regelsett("alder").apply {
            regel(virkningsdato) { virkningsdato.sisteAv(fødselsdato, søknadsdato, sisteDagMedArbeidsplikt, sisteDagMedLønn) }
        }
}
