package no.nav.dagpenger.features

import io.cucumber.java8.No
import io.kotest.assertions.withClue
import io.kotest.matchers.shouldBe
import no.nav.dagpenger.features.MeldepliktSteg.Utfall.`Ikke oppfylt`
import no.nav.dagpenger.features.MeldepliktSteg.Utfall.Oppfylt
import no.nav.dagpenger.features.utils.somLocalDate
import no.nav.dagpenger.opplysning.Faktum
import no.nav.dagpenger.opplysning.Gyldighetsperiode
import no.nav.dagpenger.opplysning.Opplysninger
import no.nav.dagpenger.opplysning.Regelkjøring
import no.nav.dagpenger.regel.Meldeplikt
import no.nav.dagpenger.regel.Søknadstidspunkt
import org.junit.jupiter.api.Assertions.assertTrue

class MeldepliktSteg : No {
    private val regelsett = listOf(Meldeplikt.regelsett, Søknadstidspunkt.regelsett)
    private val opplysninger: Opplysninger = Opplysninger()

    init {
        Gitt("at personen søkte {string}") { søknadsdato: String ->
            Regelkjøring(søknadsdato.somLocalDate(), opplysninger, *regelsett.toTypedArray())
            opplysninger.leggTil(Faktum(Søknadstidspunkt.søknadsdato, søknadsdato.somLocalDate()))
            opplysninger.leggTil(Faktum(Søknadstidspunkt.ønsketdato, søknadsdato.somLocalDate()))
        }
        Gitt("personen registrerte seg {string}") { registrert: String ->
            opplysninger.leggTil(
                Faktum(
                    Meldeplikt.registrertArbeidssøker,
                    true,
                    Gyldighetsperiode(fom = registrert.somLocalDate(), tom = registrert.somLocalDate()),
                ),
            )
        }

        Så("er kravet til meldeplikt {string}") { utfall: String ->

            withClue("Forventer at opplysninger inneholder '${Meldeplikt.registrertPåSøknadstidspunktet.navn}'") {
                opplysninger.har(Meldeplikt.registrertPåSøknadstidspunktet) shouldBe true
            }
            assertTrue(opplysninger.har(Meldeplikt.registrertPåSøknadstidspunktet))

            val verdi = opplysninger.finnOpplysning(Meldeplikt.registrertPåSøknadstidspunktet).verdi

            withClue("Forventet at kravet til meldeplikt skulle være $utfall") {
                when (Utfall.valueOf(utfall)) {
                    Oppfylt -> verdi shouldBe true
                    `Ikke oppfylt` -> verdi shouldBe false
                }
            }
        }
    }

    private enum class Utfall {
        Oppfylt,

        @Suppress("ktlint:standard:enum-entry-name-case")
        `Ikke oppfylt`,
    }
}
