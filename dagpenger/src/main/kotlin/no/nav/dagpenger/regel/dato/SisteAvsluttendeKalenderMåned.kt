package no.nav.dagpenger.regel.dato

import no.nav.dagpenger.opplysning.LesbarOpplysninger
import no.nav.dagpenger.opplysning.Opplysningstype
import no.nav.dagpenger.opplysning.regel.Regel
import java.time.LocalDate

class SisteAvsluttendeKalenderMåned internal constructor(
    produserer: Opplysningstype<LocalDate>,
    private val søknadstidspunkt: Opplysningstype<LocalDate>,
    private val rapporteringsfrist: Opplysningstype<LocalDate>,
) : Regel<LocalDate>(produserer, listOf(søknadstidspunkt, rapporteringsfrist)) {
    override fun kjør(opplysninger: LesbarOpplysninger): LocalDate {
        val a = opplysninger.finnOpplysning(søknadstidspunkt).verdi
        val b = opplysninger.finnOpplysning(rapporteringsfrist).verdi
        val månederTilbake =
            if (a.isBefore(b) || a.isEqual(b)) {
                2
            } else {
                1
            }

        val avsluttendeKalenderMåned = b.minusMonths(månederTilbake.toLong())
        return avsluttendeKalenderMåned.withDayOfMonth(avsluttendeKalenderMåned.lengthOfMonth())
    }

    override fun toString() = "Siste avsluttende kalendermåned for $søknadstidspunkt og $rapporteringsfrist"
}

fun Opplysningstype<LocalDate>.sisteAvsluttendeKalenderMåned(
    søknadstidspunkt: Opplysningstype<LocalDate>,
    rapporteringsfrist: Opplysningstype<LocalDate>,
) = SisteAvsluttendeKalenderMåned(this, søknadstidspunkt, rapporteringsfrist)
