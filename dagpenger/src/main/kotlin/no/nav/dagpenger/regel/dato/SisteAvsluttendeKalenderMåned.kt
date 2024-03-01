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
        val søknadsdato = opplysninger.finnOpplysning(søknadstidspunkt).verdi
        val rapporteringsdato = opplysninger.finnOpplysning(rapporteringsfrist).verdi
        val månederTilbake =
            when (søknadsdato.førEllerLik(rapporteringsdato)) {
                true -> 2
                false -> 1
            }

        val avsluttendeKalenderMåned = rapporteringsdato.minusMonths(månederTilbake.toLong())
        return avsluttendeKalenderMåned.withDayOfMonth(avsluttendeKalenderMåned.lengthOfMonth())
    }

    override fun toString() = "Siste avsluttende kalendermåned for $søknadstidspunkt og $rapporteringsfrist"
}

private fun LocalDate.førEllerLik(dato: LocalDate) = this.isBefore(dato) || this.isEqual(dato)

fun Opplysningstype<LocalDate>.sisteAvsluttendeKalenderMåned(
    søknadstidspunkt: Opplysningstype<LocalDate>,
    rapporteringsfrist: Opplysningstype<LocalDate>,
) = SisteAvsluttendeKalenderMåned(this, søknadstidspunkt, rapporteringsfrist)
