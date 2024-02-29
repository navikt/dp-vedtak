package no.nav.dagpenger.opplysning.regel.dato

import no.nav.dagpenger.opplysning.LesbarOpplysninger
import no.nav.dagpenger.opplysning.Opplysningstype
import no.nav.dagpenger.opplysning.regel.Regel
import java.time.LocalDate

class SisteDagIForrigeMåned internal constructor(
    produserer: Opplysningstype<LocalDate>,
    private val dato: Opplysningstype<LocalDate>,
) : Regel<LocalDate>(produserer, listOf(dato)) {
    override fun kjør(opplysninger: LesbarOpplysninger): LocalDate {
        val a = opplysninger.finnOpplysning(dato).verdi.minusMonths(1)
        return a.withDayOfMonth(a.lengthOfMonth())
    }

    override fun toString() = "Siste dag i forrige måneden for $dato"
}

fun Opplysningstype<LocalDate>.sisteDagIForrigeMåned(dato: Opplysningstype<LocalDate>) = SisteDagIForrigeMåned(this, dato)
