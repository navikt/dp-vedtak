package no.nav.dagpenger.features

import io.cucumber.java.BeforeStep
import io.cucumber.java8.No
import no.nav.dagpenger.dato.mai
import no.nav.dagpenger.features.utils.somLocalDate
import no.nav.dagpenger.opplysning.Faktum
import no.nav.dagpenger.opplysning.Opplysninger
import no.nav.dagpenger.opplysning.Regelkjøring
import no.nav.dagpenger.regel.Søknadstidspunkt
import no.nav.dagpenger.regel.TapAvArbeidsinntektOgArbeidstid
import no.nav.dagpenger.regel.TapAvArbeidsinntektOgArbeidstid.beregnetArbeidstid
import no.nav.dagpenger.regel.TapAvArbeidsinntektOgArbeidstid.kravPåLønn
import no.nav.dagpenger.regel.TapAvArbeidsinntektOgArbeidstid.kravTilTapAvArbeidsinntektOgArbeidstid
import no.nav.dagpenger.regel.TapAvArbeidsinntektOgArbeidstid.kravTilTaptArbeidstid
import no.nav.dagpenger.regel.TapAvArbeidsinntektOgArbeidstid.nyArbeidstid
import no.nav.dagpenger.regel.TapAvArbeidsinntektOgArbeidstid.tapAvArbeid
import no.nav.dagpenger.regel.TapAvArbeidsinntektOgArbeidstid.tapAvArbeidsinntekt
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue

class TapAvArbeidSteg : No {
    private val fraDato = 10.mai(2022)
    private val regelsett = listOf(TapAvArbeidsinntektOgArbeidstid.regelsett, Søknadstidspunkt.regelsett)
    private val opplysninger = Opplysninger()

    @BeforeStep
    fun kjørRegler() {
        Regelkjøring(fraDato, opplysninger, *regelsett.toTypedArray())
    }

    init {

        Gitt("at søknadsdatossssss er {string}") { søknadsdato: String ->
            opplysninger.leggTil(
                Faktum(
                    Søknadstidspunkt.søknadsdato,
                    søknadsdato.somLocalDate(),
                ),
            )
            opplysninger.leggTil(
                Faktum(
                    Søknadstidspunkt.ønsketdato,
                    søknadsdato.somLocalDate(),
                ),
            )
        }

        Gitt("at personen har tapt arbeid") {
            opplysninger.leggTil(Faktum(tapAvArbeid, true))
        }
        Og("personen har tapt arbeidsinntekt") {
            opplysninger.leggTil(Faktum(kravPåLønn, false))
        }
        Og("har fått fastsatt vanlig arbeidstid til {double}") { timer: Double ->
            opplysninger.leggTil(Faktum(beregnetArbeidstid, timer))
        }
        Og("har ny arbeidstid {double}") { timer: Double ->
            opplysninger.leggTil(Faktum(nyArbeidstid, timer))
        }
        Når("personen søker om dagpenger") { }
        Så("skal personen oppfylle kravet til tap av arbeidsinntekt") {
            assertTrue(opplysninger.har(tapAvArbeidsinntekt))
            assertTrue(opplysninger.finnOpplysning(tapAvArbeidsinntekt).verdi)
        }
        Og("personen skal {string} kravet til tap av arbeidstid") { utfall: String ->
            val verdi = utfall(utfall)
            assertTrue(opplysninger.har(kravTilTaptArbeidstid))
            assertEquals(opplysninger.finnOpplysning(kravTilTaptArbeidstid).verdi, verdi)
            assertEquals(opplysninger.finnOpplysning(kravTilTapAvArbeidsinntektOgArbeidstid).verdi, verdi)
        }
    }

    private fun utfall(verdi: String) =
        when (verdi) {
            "ikke oppfylt" -> false
            "oppfylt" -> true
            else -> throw IllegalArgumentException("Ukjent utfall")
        }
}
