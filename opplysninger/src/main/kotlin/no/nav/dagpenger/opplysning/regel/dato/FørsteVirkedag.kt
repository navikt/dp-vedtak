package no.nav.dagpenger.opplysning.regel.dato

import no.bekk.bekkopen.date.NorwegianDateUtil
import no.nav.dagpenger.opplysning.LesbarOpplysninger
import no.nav.dagpenger.opplysning.Opplysningstype
import no.nav.dagpenger.opplysning.regel.Regel
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date

class FørsteVirkedag internal constructor(
    produserer: Opplysningstype<LocalDate>,
    private val dato: Opplysningstype<LocalDate>,
) : Regel<LocalDate>(produserer, listOf(dato)) {
    override fun kjør(opplysninger: LesbarOpplysninger): LocalDate {
        val virkedag = opplysninger.finnOpplysning(dato).verdi
        return finnFørsteVirkedag(virkedag)
    }

    override fun toString() = "Finn første virkedag etter $dato"

    private tailrec fun finnFørsteVirkedag(dato: LocalDate): LocalDate {
        return if (dato.virkedag()) {
            dato
        } else {
            finnFørsteVirkedag(
                dato.plusDays(1),
            )
        }
    }

    private fun LocalDate.virkedag(): Boolean =
        NorwegianDateUtil.isWorkingDay(Date.from(this.atStartOfDay(ZoneId.systemDefault()).toInstant()))
}

fun Opplysningstype<LocalDate>.førsteVirkedag(dato: Opplysningstype<LocalDate>) = FørsteVirkedag(this, dato)
