package no.nav.dagpenger.regel

import no.nav.dagpenger.opplysning.Opplysningstype
import no.nav.dagpenger.opplysning.Regelsett
import no.nav.dagpenger.opplysning.id
import no.nav.dagpenger.opplysning.regel.alle
import no.nav.dagpenger.regel.Behov.HelseTilAlleTyperJobb
import no.nav.dagpenger.regel.Behov.KanJobbeDeltid
import no.nav.dagpenger.regel.Behov.KanJobbeHvorSomHelst
import no.nav.dagpenger.regel.Behov.VilligTilÅBytteYrke

object ReellArbeidssøker {
    internal val kanJobbeDeltid = Opplysningstype.somBoolsk("Kan jobbe heltid og deltid".id(KanJobbeDeltid))
    internal val kanJobbeHvorSomHelst = Opplysningstype.somBoolsk("Kan jobbe i hele Norge".id(KanJobbeHvorSomHelst))
    internal val helseTilAlleTyperJobb = Opplysningstype.somBoolsk("Kan ta alle typer arbeid".id(HelseTilAlleTyperJobb))
    internal val villigTilÅBytteYrke = Opplysningstype.somBoolsk("Villig til å bytte yrke".id(VilligTilÅBytteYrke))

    val kravTilArbeidssøker = Opplysningstype.somBoolsk("Krav til arbeidssøker")

    val regelsett =
        Regelsett("Reell arbeidssøker") {
            regel(kravTilArbeidssøker) {
                alle(
                    kanJobbeDeltid,
                    kanJobbeHvorSomHelst,
                    helseTilAlleTyperJobb,
                    villigTilÅBytteYrke,
                )
            }
        }
}
