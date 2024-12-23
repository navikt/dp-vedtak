package no.nav.dagpenger.opplysning.regel.dato

import no.nav.dagpenger.opplysning.LesbarOpplysninger
import no.nav.dagpenger.opplysning.Opplysningstype
import no.nav.dagpenger.opplysning.regel.Regel
import java.time.LocalDate

class DagensDato internal constructor(
    produserer: Opplysningstype<LocalDate>,
) : Regel<LocalDate>(produserer) {
    override fun lagPlan(
        opplysninger: LesbarOpplysninger,
        plan: MutableSet<Regel<*>>,
        produsenter: Map<Opplysningstype<out Comparable<*>>, Regel<*>>,
    ) {
        if (opplysninger.har(produserer)) return
        val dag = opplysninger.finnOpplysning(produserer).verdi
        val dagensDato = LocalDate.now()

        if (dagensDato != dag) {
            plan.add(this)
        }
    }

    override fun kjør(opplysninger: LesbarOpplysninger): LocalDate = LocalDate.now()

    override fun toString() = "Fastsetter $produserer til dagens dato"
}

val Opplysningstype<LocalDate>.finnDagensDato get() = DagensDato(this)
