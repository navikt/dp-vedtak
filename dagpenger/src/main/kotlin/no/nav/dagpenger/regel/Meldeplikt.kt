package no.nav.dagpenger.regel

import no.nav.dagpenger.opplysning.Opplysningstype
import no.nav.dagpenger.opplysning.Regelsett
import no.nav.dagpenger.opplysning.id
import no.nav.dagpenger.opplysning.regel.dato.førEllerLik
import no.nav.dagpenger.opplysning.regel.innhentMed
import no.nav.dagpenger.regel.Behov.RegistrertSomArbeidssøker

object Meldeplikt {
    private val søknadstidspunkt = Søknadstidspunkt.søknadstidspunkt
    internal val registrertArbeidssøker = Opplysningstype.somDato("Registrert som arbeidssøker".id(RegistrertSomArbeidssøker))
    internal val registrertPåSøknadstidspunktet = Opplysningstype.somBoolsk("Registrert som arbeidssøker på søknadstidspunktet")

    val regelsett =
        Regelsett("Meldeplikt") {
            regel(registrertArbeidssøker) { innhentMed(søknadstidspunkt) }
            regel(registrertPåSøknadstidspunktet) { førEllerLik(registrertArbeidssøker, søknadstidspunkt) }
        }
}
