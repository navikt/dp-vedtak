package no.nav.dagpenger.features

import io.cucumber.java.BeforeStep
import io.cucumber.java8.No
import no.nav.dagpenger.dato.mai
import no.nav.dagpenger.opplysning.Faktum
import no.nav.dagpenger.opplysning.Opplysninger
import no.nav.dagpenger.opplysning.Regelkjøring
import no.nav.dagpenger.regel.ReellArbeidssøker
import no.nav.dagpenger.regel.Søknadstidspunkt
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue

class ReellArbeidssøkerSteg : No {
    private val fraDato = 10.mai(2022)
    private val regelsett = listOf(ReellArbeidssøker.regelsett, Søknadstidspunkt.regelsett)
    private val opplysninger: Opplysninger =
        Opplysninger(
            regelsett.flatMap { it.startverdier() },
        )

    @BeforeStep
    fun kjørRegler() {
        Regelkjøring(fraDato, opplysninger, *regelsett.toTypedArray())
    }

    init {

        Gitt("at personen søker dagpenger") {
            opplysninger.leggTil(Faktum(Søknadstidspunkt.søknadsdato, 11.mai(2022)))
            opplysninger.leggTil(Faktum(Søknadstidspunkt.ønsketdato, 11.mai(2022)))
        }
        Gitt("kan jobbe både heltid og deltid") {
            opplysninger.leggTil(Faktum(ReellArbeidssøker.kanJobbeDeltid, true))
        }
        Gitt("kan ikke jobbe både heltid og deltid") {
            opplysninger.leggTil(Faktum(ReellArbeidssøker.kanJobbeDeltid, false))
        }
        Gitt("kan jobbe i hele Norge") {
            opplysninger.leggTil(Faktum(ReellArbeidssøker.kanJobbeHvorSomHelst, true))
        }
        Gitt("kan ikke jobbe i hele Norge") {
            opplysninger.leggTil(Faktum(ReellArbeidssøker.kanJobbeHvorSomHelst, false))
        }
        Gitt("kan ta alle typer arbeid") {
            opplysninger.leggTil(Faktum(ReellArbeidssøker.helseTilAlleTyperJobb, true))
        }
        Gitt("kan ikke ta alle typer arbeid") {
            opplysninger.leggTil(Faktum(ReellArbeidssøker.helseTilAlleTyperJobb, false))
        }
        Gitt("oppfyller kravet til unntak for mobilitet") {
            opplysninger.leggTil(Faktum(ReellArbeidssøker.unntakMobilitet, true))
        }
        Gitt("er villig til å bytte yrke eller gå ned i lønn") {
            opplysninger.leggTil(Faktum(ReellArbeidssøker.villigTilÅBytteYrke, true))
        }
        Gitt("er ikke villig til å bytte yrke eller gå ned i lønn") {
            opplysninger.leggTil(Faktum(ReellArbeidssøker.villigTilÅBytteYrke, false))
        }

        Så("skal kravet til reell arbeidssøker være oppfylt") {
            assertTrue(opplysninger.har(ReellArbeidssøker.kravTilArbeidssøker))
            val verdi = opplysninger.finnOpplysning(ReellArbeidssøker.kravTilArbeidssøker).verdi
            assertTrue(verdi)
        }
        Så("skal kravet til reell arbeidssøker ikke være oppfylt") {
            assertTrue(opplysninger.har(ReellArbeidssøker.kravTilArbeidssøker))
            val verdi = opplysninger.finnOpplysning(ReellArbeidssøker.kravTilArbeidssøker).verdi
            assertFalse(verdi)
        }
    }
}
