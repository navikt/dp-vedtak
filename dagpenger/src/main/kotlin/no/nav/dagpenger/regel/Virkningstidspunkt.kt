package no.nav.dagpenger.regel

import no.nav.dagpenger.avklaring.Kontrollpunkt
import no.nav.dagpenger.opplysning.Opplysningstype
import no.nav.dagpenger.opplysning.Regelsett
import no.nav.dagpenger.opplysning.regel.dato.finnDagensDato
import no.nav.dagpenger.opplysning.regel.fraOgMed

object Virkningstidspunkt {
    val virkningstidspunkt = Opplysningstype.somDato("EttBeregnetVirkningstidspunkt")

    val dagensDato = Opplysningstype.somDato("Dagens dato")

    val regelsett =
        Regelsett(
            "3A-1 Søknadstidspunkt",
            "Dagpengeforskriften § 3A-1. Søknadstidspunkt",
        ).apply {
            regel(dagensDato) { finnDagensDato }
            regel(virkningstidspunkt) { fraOgMed(KravPåDagpenger.kravPåDagpenger) }
        }

    val VirkningstidspunktForLangtFramITid =
        Kontrollpunkt(Avklaringspunkter.VirkningstidspunktForLangtFramITid) {
            it.har(virkningstidspunkt) &&
                it.har(dagensDato) &&
                it.finnOpplysning(virkningstidspunkt).verdi.isAfter(
                    it.finnOpplysning(dagensDato).verdi.plusDays(14),
                )
        }
}
