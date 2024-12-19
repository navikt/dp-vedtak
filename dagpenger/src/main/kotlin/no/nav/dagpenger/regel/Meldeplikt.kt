package no.nav.dagpenger.regel

import no.nav.dagpenger.avklaring.Kontrollpunkt
import no.nav.dagpenger.opplysning.Opplysningstype
import no.nav.dagpenger.opplysning.Regelsett
import no.nav.dagpenger.opplysning.id
import no.nav.dagpenger.opplysning.regel.erSann
import no.nav.dagpenger.opplysning.regel.innhentMed
import no.nav.dagpenger.regel.Behov.RegistrertSomArbeidssøker
import no.nav.dagpenger.regel.Søknadstidspunkt.prøvingsdato

object Meldeplikt {
    internal val registrertArbeidssøker = Opplysningstype.somBoolsk("Registrert som arbeidssøker".id(RegistrertSomArbeidssøker))
    val registrertPåSøknadstidspunktet = Opplysningstype.somBoolsk("Registrert som arbeidssøker på søknadstidspunktet")

    val regelsett =
        Regelsett("Meldeplikt") {
            regel(registrertArbeidssøker) { innhentMed(prøvingsdato) }
            regel(registrertPåSøknadstidspunktet) { erSann(registrertArbeidssøker) }
        }

    val IkkeRegistrertSomArbeidsøkerKontroll =
        Kontrollpunkt(Avklaringspunkter.IkkeRegistrertSomArbeidsøker) {
            it.har(registrertPåSøknadstidspunktet) && !it.finnOpplysning(registrertPåSøknadstidspunktet).verdi
        }
}
