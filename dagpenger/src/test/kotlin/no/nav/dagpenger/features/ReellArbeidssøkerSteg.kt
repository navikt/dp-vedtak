package no.nav.dagpenger.features

import io.cucumber.java.BeforeStep
import io.cucumber.java8.No
import io.kotest.matchers.shouldBe
import no.nav.dagpenger.dato.mai
import no.nav.dagpenger.opplysning.Faktum
import no.nav.dagpenger.opplysning.Opplysninger
import no.nav.dagpenger.opplysning.Regelkjøring
import no.nav.dagpenger.regel.ReellArbeidssøker
import no.nav.dagpenger.regel.Søknadstidspunkt

class ReellArbeidssøkerSteg : No {
    private val fraDato = 10.mai(2022)
    private val regelsett = listOf(ReellArbeidssøker.regelsett, Søknadstidspunkt.regelsett)
    private val opplysninger: Opplysninger = Opplysninger()
    private lateinit var regelkjøring: Regelkjøring

    @BeforeStep
    fun kjørRegler() {
        regelkjøring = Regelkjøring(fraDato, opplysninger, *regelsett.toTypedArray())
    }

    init {
        Gitt("at personen søker dagpenger") {
            opplysninger.leggTil(Faktum(Søknadstidspunkt.søknadsdato, 11.mai(2022)))
            opplysninger.leggTil(Faktum(Søknadstidspunkt.ønsketdato, 11.mai(2022)))
        }
        Gitt("kan jobbe både heltid og deltid") {
            opplysninger.leggTil(Faktum(ReellArbeidssøker.kanJobbeDeltid, true))
            opplysninger.leggTil(Faktum(ReellArbeidssøker.ønsketArbeidstid, 40.0))
        }
        Gitt("kan ikke jobbe både heltid og deltid") {
            opplysninger.leggTil(Faktum(ReellArbeidssøker.kanJobbeDeltid, false))
            opplysninger.leggTil(Faktum(ReellArbeidssøker.ønsketArbeidstid, 25.0))
        }
        Gitt("kan jobbe i hele Norge") {
            opplysninger.leggTil(Faktum(ReellArbeidssøker.kanJobbeHvorSomHelst, true))
        }
        Gitt("kan ikke jobbe i hele Norge") {
            opplysninger.leggTil(Faktum(ReellArbeidssøker.kanJobbeHvorSomHelst, false))
        }
        Gitt("kan ta alle typer arbeid") {
            opplysninger.leggTil(Faktum(ReellArbeidssøker.helseTilAlleTyperArbeid, true))
        }
        Gitt("kan ikke ta alle typer arbeid") {
            opplysninger.leggTil(Faktum(ReellArbeidssøker.helseTilAlleTyperArbeid, false))
        }
        Men("oppfyller kravet å kun søke lokalt arbeid") {
            opplysninger.leggTil(Faktum(ReellArbeidssøker.godkjentLokalArbeidssøker, true))
        }
        Men("oppfyller kravet til å kun søke deltidssarbeid") {
            opplysninger.leggTil(Faktum(ReellArbeidssøker.godkjentDeltidssøker, true))
        }
        Gitt("er villig til å bytte yrke eller gå ned i lønn") {
            opplysninger.leggTil(Faktum(ReellArbeidssøker.villigTilEthvertArbeid, true))
        }
        Gitt("er ikke villig til å bytte yrke eller gå ned i lønn") {
            opplysninger.leggTil(Faktum(ReellArbeidssøker.villigTilEthvertArbeid, false))
        }

        Så("skal kravet til reell arbeidssøker være oppfylt") {
            regelkjøring.evaluer()

            opplysninger.har(ReellArbeidssøker.kravTilArbeidssøker) shouldBe true
            opplysninger.finnOpplysning(ReellArbeidssøker.kravTilArbeidssøker).verdi shouldBe true
        }
        Så("skal kravet til reell arbeidssøker ikke være oppfylt") {
            regelkjøring.evaluer()

            opplysninger.har(ReellArbeidssøker.kravTilArbeidssøker) shouldBe true
            opplysninger.finnOpplysning(ReellArbeidssøker.kravTilArbeidssøker).verdi shouldBe false
        }
    }
}
