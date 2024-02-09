package no.nav.dagpenger.behandling.regel.dato

import no.nav.dagpenger.behandling.LesbarOpplysninger
import no.nav.dagpenger.behandling.Opplysningstype
import no.nav.dagpenger.behandling.regel.Regel
import java.time.LocalDate

class SisteDagIMåned internal constructor(
    produserer: Opplysningstype<LocalDate>,
    private val dato: Opplysningstype<LocalDate>,
) : Regel<LocalDate>(produserer, listOf(dato)) {
    override fun kjør(opplysninger: LesbarOpplysninger): LocalDate {
        val a = opplysninger.finnOpplysning(dato).verdi
        return a.withDayOfMonth(a.lengthOfMonth())
    }

    override fun toString() = "Siste dag i måneden for $dato"
}

fun Opplysningstype<LocalDate>.sisteDagIMåned(dato: Opplysningstype<LocalDate>) = SisteDagIMåned(this, dato)
