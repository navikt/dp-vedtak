package no.nav.dagpenger.regel

import no.nav.dagpenger.opplysning.Opplysningstype
import no.nav.dagpenger.opplysning.Regelsett
import no.nav.dagpenger.opplysning.id
import no.nav.dagpenger.opplysning.regel.innhentMed

object Verneplikt {
    val avtjentVerneplikt = Opplysningstype.somBoolsk("Avtjent verneplikt".id("Verneplikt"))

    val regelsett =
        Regelsett("Verneplikt") {
            regel(avtjentVerneplikt) { innhentMed() }
        }
}
