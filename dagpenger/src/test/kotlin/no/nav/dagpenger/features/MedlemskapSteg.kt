package no.nav.dagpenger.features

import io.cucumber.java.BeforeStep
import io.cucumber.java8.No
import io.kotest.assertions.withClue
import io.kotest.matchers.shouldBe
import no.nav.dagpenger.dato.mai
import no.nav.dagpenger.opplysning.Faktum
import no.nav.dagpenger.opplysning.Opplysning
import no.nav.dagpenger.opplysning.Opplysninger
import no.nav.dagpenger.opplysning.Regelkjøring
import no.nav.dagpenger.regel.Medlemskap
import no.nav.dagpenger.regel.Søknadstidspunkt
import java.time.LocalDate

class MedlemskapSteg : No {
    private val fraDato = 23.mai(2024)
    private val regelsett = listOf(Medlemskap.regelsett)
    private val opplysninger = Opplysninger()
    private lateinit var regelkjøring: Regelkjøring

    @BeforeStep
    fun kjørRegler() {
        regelkjøring = Regelkjøring(fraDato, opplysninger, *regelsett.toTypedArray())
    }

    init {
        Gitt("at søker har søkt om dagpenger og er medlem?") {
            regelkjøring.leggTil(Faktum<LocalDate>(Søknadstidspunkt.søknadstidspunkt, fraDato) as Opplysning<*>)
        }

        Og("at personen er medlem {boolsk} i folketrygden") { medlem: Boolean ->
            regelkjøring.leggTil(Faktum<Boolean>(Medlemskap.medlemFolketrygden, medlem) as Opplysning<*>)
        }

        Så("skal vilkåret om medlemskap være {boolsk}") { utfall: Boolean ->
            val faktum = opplysninger.finnOpplysning(Medlemskap.oppfyllerMedlemskap)
            withClue("Vilkåret om medlemskap skal være $utfall") {
                faktum.verdi shouldBe utfall
            }
        }
    }
}
