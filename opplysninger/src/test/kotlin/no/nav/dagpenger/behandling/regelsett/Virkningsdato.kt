package no.nav.dagpenger.behandling.regelsett

import no.nav.dagpenger.behandling.Opplysningstype
import no.nav.dagpenger.behandling.Regelsett
import no.nav.dagpenger.behandling.regel.dato.sisteAv
import no.nav.dagpenger.behandling.regelsett.Alderskrav.fødselsdato
import java.time.LocalDate

object Virkningsdato {
    // val fødselsdato = Opplysningstype<LocalDate>("Fødselsdato")
    val søknadsdato = Opplysningstype<LocalDate>("Søknadsdato")
    val sisteDagMedArbeidsplikt = Opplysningstype<LocalDate>("Siste dag med arbeidsplikt")
    val sisteDagMedLønn = Opplysningstype<LocalDate>("Siste dag med lønn")

    val virkningsdato = Opplysningstype<LocalDate>("Virkningsdato")

    val regelsett =
        Regelsett("alder").apply {
            sisteAv(virkningsdato, fødselsdato, søknadsdato, sisteDagMedArbeidsplikt, sisteDagMedLønn)
        }
}
