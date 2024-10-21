package no.nav.dagpenger.opplysning.regel.dato

import no.nav.dagpenger.opplysning.LesbarOpplysninger
import no.nav.dagpenger.opplysning.Opplysningstype
import no.nav.dagpenger.opplysning.regel.Regel
import java.time.LocalDate

class DagensDato internal constructor(
    produserer: Opplysningstype<LocalDate>,
) : Regel<LocalDate>(produserer) {
    override fun kanKjøre(opplysninger: LesbarOpplysninger): Boolean {
        if (!opplysninger.har(produserer)) return true
        val dag = opplysninger.finnOpplysning(produserer).verdi
        val dagensDato = LocalDate.now()

        return dagensDato != dag
    }

    override fun kjør(opplysninger: LesbarOpplysninger): LocalDate = LocalDate.now()

    override fun toString() = "Fastsetter $produserer til dagens dato"
}

val Opplysningstype<LocalDate>.finnDagensDato get() = DagensDato(this)
