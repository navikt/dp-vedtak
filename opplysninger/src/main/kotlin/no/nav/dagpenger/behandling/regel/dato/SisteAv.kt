package no.nav.dagpenger.behandling.regel.dato

import no.nav.dagpenger.behandling.LesbarOpplysninger
import no.nav.dagpenger.behandling.Opplysningstype
import no.nav.dagpenger.behandling.Regelsett
import no.nav.dagpenger.behandling.regel.Regel
import java.time.LocalDate

internal class SisteAv(
    produserer: Opplysningstype<LocalDate>,
    private vararg val datoer: Opplysningstype<LocalDate>,
) : Regel<LocalDate>(produserer, datoer.toList()) {
    override fun kjør(opplysninger: LesbarOpplysninger): LocalDate {
        val dager = opplysninger.finnAlle(datoer.toList()).map { it.verdi as LocalDate }
        return dager.maxOrNull() ?: throw IllegalStateException("Ingen datoer funnet")
    }

    override fun toString() = "Siste dato av $datoer"
}

fun Regelsett.sisteAv(
    gjelderFra: LocalDate,
    produserer: Opplysningstype<LocalDate>,
    vararg datoer: Opplysningstype<LocalDate>,
): Regel<LocalDate> {
    return SisteAv(produserer, *datoer).also { leggTil(gjelderFra, produserer, it) }
}

fun Regelsett.sisteAv(
    produserer: Opplysningstype<LocalDate>,
    vararg datoer: Opplysningstype<LocalDate>,
) = sisteAv(LocalDate.MIN, produserer, *datoer)
