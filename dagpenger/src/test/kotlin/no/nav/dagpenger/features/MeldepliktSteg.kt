package no.nav.dagpenger.features

import io.cucumber.java8.No
import no.nav.dagpenger.dato.mai
import no.nav.dagpenger.features.MeldepliktSteg.Utfall.`Ikke oppfylt`
import no.nav.dagpenger.features.MeldepliktSteg.Utfall.Oppfylt
import no.nav.dagpenger.features.utils.somLocalDate
import no.nav.dagpenger.opplysning.Faktum
import no.nav.dagpenger.opplysning.Opplysninger
import no.nav.dagpenger.opplysning.Regelkjøring
import no.nav.dagpenger.regel.Meldeplikt
import no.nav.dagpenger.regel.Søknadstidspunkt
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue

class MeldepliktSteg : No {
    private val fraDato = 10.mai(2022).atStartOfDay()
    private val regelsett = listOf(Meldeplikt.regelsett, Søknadstidspunkt.regelsett)
    private val opplysninger: Opplysninger = Opplysninger()

    init {
        Regelkjøring(fraDato, opplysninger, *regelsett.toTypedArray())

        Gitt("at personen søkte {string}") { søknadsdato: String ->
            opplysninger.leggTil(Faktum(Søknadstidspunkt.søknadsdato, søknadsdato.somLocalDate()))
            opplysninger.leggTil(Faktum(Søknadstidspunkt.ønsketdato, søknadsdato.somLocalDate()))
        }
        Gitt("personen registrerte seg {string}") { registrert: String ->
            opplysninger.leggTil(Faktum(Meldeplikt.registrertArbeidssøker, registrert.somLocalDate()))
        }

        Så("er kravet til meldeplikt {string}") { utfall: String ->
            assertTrue(opplysninger.har(Meldeplikt.registrertPåSøknadstidspunktet))

            val verdi = opplysninger.finnOpplysning(Meldeplikt.registrertPåSøknadstidspunktet).verdi
            when (Utfall.valueOf(utfall)) {
                Oppfylt -> assertTrue(verdi)
                `Ikke oppfylt` -> assertFalse(verdi)
            }
        }
    }

    private enum class Utfall {
        Oppfylt,

        @Suppress("ktlint:standard:enum-entry-name-case")
        `Ikke oppfylt`,
    }
}
