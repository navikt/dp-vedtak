package no.nav.dagpenger.regel

import no.nav.dagpenger.opplysning.Opplysningstype
import no.nav.dagpenger.opplysning.Regelsett
import no.nav.dagpenger.opplysning.id
import no.nav.dagpenger.opplysning.regel.alle
import no.nav.dagpenger.opplysning.regel.dato.førEllerLik
import no.nav.dagpenger.opplysning.regel.innhentMed
import no.nav.dagpenger.regel.Behov.HelseTilAlleTyperJobb
import no.nav.dagpenger.regel.Behov.KanJobbeDeltid
import no.nav.dagpenger.regel.Behov.KanJobbeHvorSomHelst
import no.nav.dagpenger.regel.Behov.RegistrertSomArbeidssøker
import no.nav.dagpenger.regel.Behov.VilligTilÅBytteYrke

object ReellArbeidssøker {
    private val søknadstidspunkt = Søknadstidspunkt.søknadstidspunkt
    private val registrertArbeidssøker = Opplysningstype.somDato("Registrert som arbeidssøker".id(RegistrertSomArbeidssøker))
    private val registrertPåSøknadstidspunktet = Opplysningstype.somBoolsk("Registrert som arbeidssøker på søknadstidspunktet")

    private val kanJobbeDeltid = Opplysningstype.somBoolsk("Kan jobbe heltid og deltid".id(KanJobbeDeltid))
    private val kanJobbeHvorSomHelst = Opplysningstype.somBoolsk("Kan jobbe i hele Norge".id(KanJobbeHvorSomHelst))
    private val helseTilAlleTyperJobb = Opplysningstype.somBoolsk("Kan ta alle typer arbeid".id(HelseTilAlleTyperJobb))
    private val villigTilÅBytteYrke = Opplysningstype.somBoolsk("Villig til å bytte yrke".id(VilligTilÅBytteYrke))

    val kravTilArbeidssøker = Opplysningstype.somBoolsk("Krav til arbeidssøker")

    val regelsett =
        Regelsett("Reell arbeidssøker") {
            regel(registrertArbeidssøker) { innhentMed(søknadstidspunkt) }
            regel(registrertPåSøknadstidspunktet) { førEllerLik(registrertArbeidssøker, søknadstidspunkt) }
            regel(kravTilArbeidssøker) {
                alle(
                    registrertPåSøknadstidspunktet,
                    kanJobbeDeltid,
                    kanJobbeHvorSomHelst,
                    helseTilAlleTyperJobb,
                    villigTilÅBytteYrke,
                )
            }
        }
}
