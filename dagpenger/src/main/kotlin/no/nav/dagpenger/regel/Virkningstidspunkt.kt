package no.nav.dagpenger.regel

import no.nav.dagpenger.opplysning.Opplysningstype
import no.nav.dagpenger.opplysning.Regelsett
import no.nav.dagpenger.opplysning.regel.fraOgMed

object Virkningstidspunkt {
    val virkningstidspunkt = Opplysningstype.somDato("EttBeregnetVirkningstidspunkt")

    val regelsett =
        Regelsett("Virkningstidspunkt").apply {
            regel(virkningstidspunkt) { fraOgMed(KravPåDagpenger.kravPåDagpenger) }
        }
}
