package no.nav.dagpenger.regel

import no.nav.dagpenger.opplysning.Opplysningstype
import no.nav.dagpenger.opplysning.Regelsett
import no.nav.dagpenger.opplysning.id
import no.nav.dagpenger.opplysning.regel.innhentMed

object Rettighetstype {
    val rettighetstype = Opplysningstype.somDato("Rettighetstype".id("Rettighetstype"))

    val regelsett =
        Regelsett("Rettighetstype") {
            regel(rettighetstype) { innhentMed() }
        }
}
