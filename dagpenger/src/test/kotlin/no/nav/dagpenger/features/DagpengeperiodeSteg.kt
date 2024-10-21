package no.nav.dagpenger.features

import io.cucumber.java.BeforeStep
import io.cucumber.java8.No
import io.kotest.matchers.shouldBe
import no.nav.dagpenger.dato.mai
import no.nav.dagpenger.opplysning.Faktum
import no.nav.dagpenger.opplysning.Opplysning
import no.nav.dagpenger.opplysning.Opplysninger
import no.nav.dagpenger.opplysning.Regelkjøring
import no.nav.dagpenger.opplysning.verdier.Beløp
import no.nav.dagpenger.regel.Minsteinntekt
import no.nav.dagpenger.regel.SøknadInnsendtHendelse.Companion.prøvingsdato
import no.nav.dagpenger.regel.Søknadstidspunkt
import no.nav.dagpenger.regel.fastsetting.Dagpengeperiode
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class DagpengeperiodeSteg : No {
    private val fraDato = 10.mai(2022)
    private val regelsett = listOf(Dagpengeperiode.regelsett, Minsteinntekt.regelsett)
    private val opplysninger: Opplysninger = Opplysninger()
    private lateinit var regelkjøring: Regelkjøring

    @BeforeStep
    fun kjørRegler() {
        regelkjøring = Regelkjøring(fraDato, opplysninger, *regelsett.toTypedArray())
    }

    init {

        Gitt("at søker har har rett til dagpenger fra {string}") { dato: String ->
            val dato = LocalDate.parse(dato, DateTimeFormatter.ofPattern("dd.MM.yyyy"))
            regelkjøring.leggTil(
                Faktum<LocalDate>(
                    Søknadstidspunkt.søknadstidspunkt,
                    dato,
                ) as Opplysning<*>,
            )
            regelkjøring.leggTil(
                Faktum<LocalDate>(
                    prøvingsdato,
                    dato,
                ) as Opplysning<*>,
            )
        }
        Gitt("at søker har {string} siste 12 måneder") { inntekt: String ->
            regelkjøring.leggTil(
                Faktum<Beløp>(
                    Minsteinntekt.inntekt12,
                    Beløp(inntekt.toBigDecimal()),
                ) as Opplysning<*>,
            )
        }

        Gitt("at søker har {string} siste 36 måneder") { inntekt: String ->
            regelkjøring.leggTil(
                Faktum<Beløp>(
                    Minsteinntekt.inntekt36,
                    Beløp(inntekt.toBigDecimal()),
                ) as Opplysning<*>,
            )
        }

        Så("skal søker ha {int} uker med dagpenger") { uker: Int ->
            val faktum = opplysninger.finnOpplysning(Dagpengeperiode.antallStønadsuker)
            faktum.verdi shouldBe uker
        }
    }
}
