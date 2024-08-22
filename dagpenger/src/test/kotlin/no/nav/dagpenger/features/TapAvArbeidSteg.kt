package no.nav.dagpenger.features

import io.cucumber.java.BeforeStep
import io.cucumber.java8.No
import no.nav.dagpenger.dato.mai
import no.nav.dagpenger.features.utils.somLocalDate
import no.nav.dagpenger.opplysning.Faktum
import no.nav.dagpenger.opplysning.Opplysning
import no.nav.dagpenger.opplysning.Opplysninger
import no.nav.dagpenger.opplysning.Regelkjøring
import no.nav.dagpenger.regel.Søknadstidspunkt
import no.nav.dagpenger.regel.TapAvArbeidsinntektOgArbeidstid
import no.nav.dagpenger.regel.TapAvArbeidsinntektOgArbeidstid.beregnetArbeidstid
import no.nav.dagpenger.regel.TapAvArbeidsinntektOgArbeidstid.beregningsregel6mnd
import no.nav.dagpenger.regel.TapAvArbeidsinntektOgArbeidstid.kravPåLønn
import no.nav.dagpenger.regel.TapAvArbeidsinntektOgArbeidstid.kravTilTapAvArbeidsinntektOgArbeidstid
import no.nav.dagpenger.regel.TapAvArbeidsinntektOgArbeidstid.kravTilTaptArbeidstid
import no.nav.dagpenger.regel.TapAvArbeidsinntektOgArbeidstid.nyArbeidstid
import no.nav.dagpenger.regel.TapAvArbeidsinntektOgArbeidstid.tapAvArbeid
import no.nav.dagpenger.regel.TapAvArbeidsinntektOgArbeidstid.tapAvArbeidsinntekt
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import java.time.LocalDate

class TapAvArbeidSteg : No {
    private val fraDato = 10.mai(2022)
    private val regelsett = listOf(TapAvArbeidsinntektOgArbeidstid.regelsett, Søknadstidspunkt.regelsett)
    private val opplysninger = Opplysninger()

    private lateinit var regelkjøring: Regelkjøring

    @BeforeStep
    fun kjørRegler() {
        regelkjøring = Regelkjøring(fraDato, opplysninger, *regelsett.toTypedArray())
    }

    init {

        Gitt("at søknadsdatossssss er {string}") { søknadsdato: String ->
            regelkjøring.leggTil(
                Faktum<LocalDate>(
                    Søknadstidspunkt.søknadsdato,
                    søknadsdato.somLocalDate(),
                ) as Opplysning<*>,
            )
            regelkjøring.leggTil(
                Faktum<LocalDate>(
                    Søknadstidspunkt.ønsketdato,
                    søknadsdato.somLocalDate(),
                ) as Opplysning<*>,
            )
        }

        Gitt("at personen har tapt arbeid") {
            regelkjøring.leggTil(Faktum(tapAvArbeid, true))
        }
        Og("personen har tapt arbeidsinntekt") {
            regelkjøring.leggTil(Faktum(kravPåLønn, false))
        }
        Og("har fått fastsatt vanlig arbeidstid til {double}") { timer: Double ->
            regelkjøring.leggTil(Faktum(beregnetArbeidstid, timer))
            regelkjøring.leggTil(Faktum(beregningsregel6mnd, true))
        }
        Og("har ny arbeidstid {double}") { timer: Double ->
            regelkjøring.leggTil(Faktum(nyArbeidstid, timer))
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
