package no.nav.dagpenger.behandling.regel.dato

import no.nav.dagpenger.behandling.LesbarOpplysninger
import no.nav.dagpenger.behandling.Opplysningstype
import no.nav.dagpenger.behandling.regel.Regel
import java.time.LocalDate

class SisteAv internal constructor(
    produserer: Opplysningstype<LocalDate>,
    private vararg val datoer: Opplysningstype<LocalDate>,
) : Regel<LocalDate>(produserer, datoer.toList()) {
    override fun kjør(opplysninger: LesbarOpplysninger): LocalDate {
        val dager = opplysninger.finnAlle(datoer.toList()).map { it.verdi as LocalDate }
        return dager.maxOrNull() ?: throw IllegalStateException("Ingen datoer funnet")
    }

    override fun toString() = "Siste dato av $datoer"
}

fun Opplysningstype<LocalDate>.sisteAv(vararg liste: Opplysningstype<LocalDate>) = SisteAv(this, *liste)
