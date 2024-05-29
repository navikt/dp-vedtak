package no.nav.dagpenger.features

import io.cucumber.java.BeforeStep
import io.cucumber.java8.No
import no.nav.dagpenger.dato.mai
import no.nav.dagpenger.features.utils.somLocalDate
import no.nav.dagpenger.opplysning.Faktum
import no.nav.dagpenger.opplysning.Opplysninger
import no.nav.dagpenger.opplysning.Regelkjøring
import no.nav.dagpenger.regel.Opphold
import no.nav.dagpenger.regel.Søknadstidspunkt
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue

class OppholdINorgeSteg : No {
    private val fraDato = 10.mai(2022)
    private val regelsett = listOf(Opphold.regelsett, Søknadstidspunkt.regelsett)
    private val opplysninger = Opplysninger()

    @BeforeStep
    fun kjørRegler() {
        Regelkjøring(fraDato, opplysninger, *regelsett.toTypedArray())
    }

    init {

        Gitt("at søknadsdatos er {string}") { søknadsdato: String ->
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

        Gitt("at personen oppholder seg i Norge") {
            opplysninger.leggTil(Faktum(Opphold.oppholdINorge, true))
        }
        Så("skal vilkåret om opphold i Norge være oppfylt") {
            assertTrue(opplysninger.har(Opphold.oppfyllerKravet))
            assertTrue(opplysninger.finnOpplysning(Opphold.oppfyllerKravet).verdi)
        }
        Gitt("at personen oppholder seg ikke i Norge") {
            opplysninger.leggTil(Faktum(Opphold.oppholdINorge, false))
        }
        Så("skal vilkåret om opphold i Norge ikke være oppfylt") {
            assertTrue(opplysninger.har(Opphold.oppfyllerKravet))
            assertFalse(opplysninger.finnOpplysning(Opphold.oppfyllerKravet).verdi)
        }
        Men("at personen oppfyller ett unntak for opphold") {
            opplysninger.leggTil(Faktum(Opphold.unntakForOpphold, true))
        }
    }
}
