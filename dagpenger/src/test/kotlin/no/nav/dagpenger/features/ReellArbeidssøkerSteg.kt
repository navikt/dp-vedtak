package no.nav.dagpenger.features

import io.cucumber.java8.No
import no.nav.dagpenger.dato.mai
import no.nav.dagpenger.features.utils.somLocalDate
import no.nav.dagpenger.opplysning.Faktum
import no.nav.dagpenger.opplysning.Opplysninger
import no.nav.dagpenger.opplysning.Regelkjøring
import no.nav.dagpenger.regel.ReellArbeidssøker
import no.nav.dagpenger.regel.Søknadstidspunkt
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue

class ReellArbeidssøkerSteg : No {
    private val fraDato = 10.mai(2022).atStartOfDay()
    private val regelsett = listOf(ReellArbeidssøker.regelsett, Søknadstidspunkt.regelsett)
    private val opplysninger: Opplysninger = Opplysninger()

    init {
        Regelkjøring(fraDato, opplysninger, *regelsett.toTypedArray())

        Gitt("at personen søkte {string}") { søknadsdato: String ->
            opplysninger.leggTil(Faktum(Søknadstidspunkt.søknadsdato, søknadsdato.somLocalDate()))
            opplysninger.leggTil(Faktum(Søknadstidspunkt.ønsketdato, søknadsdato.somLocalDate()))
        }
        Gitt("personen registrerte seg {string}") { registrert: String ->
            opplysninger.leggTil(Faktum(ReellArbeidssøker.registrertArbeidssøker, registrert.somLocalDate()))
        }
        Gitt("kan jobbe både heltid og deltid") {
            opplysninger.leggTil(Faktum(ReellArbeidssøker.kanJobbeDeltid, true))
        }
        Gitt("kan jobbe i hele Norge") {
            opplysninger.leggTil(Faktum(ReellArbeidssøker.kanJobbeHvorSomHelst, true))
        }
        Gitt("kan ta alle typer arbeid") {
            opplysninger.leggTil(Faktum(ReellArbeidssøker.helseTilAlleTyperJobb, true))
        }
        Gitt("er villig til å bytte yrke eller gå ned i lønn") {
            opplysninger.leggTil(Faktum(ReellArbeidssøker.villigTilÅBytteYrke, true))
        }

        Så("skal utfallet for kravet til reell arbeidssøker være {string}") { utfall: String ->
            assertTrue(opplysninger.har(ReellArbeidssøker.kravTilArbeidssøker))

            val verdi = opplysninger.finnOpplysning(ReellArbeidssøker.kravTilArbeidssøker).verdi

            when (Utfall.valueOf(utfall)) {
                Utfall.Ja -> assertTrue(verdi)
                Utfall.Nei -> assertFalse(verdi)
            }
        }
    }
}

private enum class Utfall {
    Ja,
    Nei,
}
