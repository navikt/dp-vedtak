package no.nav.dagpenger.features

import io.cucumber.java.BeforeStep
import io.cucumber.java8.No
import io.kotest.matchers.shouldBe
import no.nav.dagpenger.dato.mai
import no.nav.dagpenger.opplysning.Faktum
import no.nav.dagpenger.opplysning.Opplysninger
import no.nav.dagpenger.opplysning.Regelkjøring
import no.nav.dagpenger.opplysning.verdier.Beløp
import no.nav.dagpenger.regel.Søknadstidspunkt
import no.nav.dagpenger.regel.fastsetting.Dagpengegrunnlag
import no.nav.dagpenger.regel.fastsetting.VernepliktFastsetting
import java.time.LocalDate

class VernepliktFastsettingSteg : No {
    private val fraDato = 10.mai(2022)
    private val regelsett = listOf(VernepliktFastsetting.regelsett, Dagpengegrunnlag.regelsett)
    private val opplysninger: Opplysninger = Opplysninger()
    private lateinit var regelkjøring: Regelkjøring

    @BeforeStep
    fun kjørRegler() {
        regelkjøring = Regelkjøring(fraDato, opplysninger, *regelsett.toTypedArray())
    }

    init {
        Gitt(
            "at søker har søkt om dagpenger under verneplikt {dato}",
        ) { dato: LocalDate ->
            opplysninger.leggTil(
                Faktum(
                    Søknadstidspunkt.søknadstidspunkt,
                    dato,
                ),
            )
        }

        Så("skal grunnlag være {int}") { grunnlag: Int ->
            opplysninger.finnOpplysning(VernepliktFastsetting.vernepliktGrunnlag).verdi shouldBe Beløp(grunnlag)
        }

        Så("dagpengerperioden være {int} uker") { antallUker: Int ->
            opplysninger.finnOpplysning(VernepliktFastsetting.vernepliktPeriode).verdi shouldBe antallUker
        }
    }
}
