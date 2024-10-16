package no.nav.dagpenger.features

import io.cucumber.java.BeforeStep
import io.cucumber.java8.No
import io.kotest.matchers.shouldBe
import no.nav.dagpenger.dato.mai
import no.nav.dagpenger.opplysning.Faktum
import no.nav.dagpenger.opplysning.Opplysninger
import no.nav.dagpenger.opplysning.Regelkjøring
import no.nav.dagpenger.opplysning.verdier.Beløp
import no.nav.dagpenger.regel.RegelverkDagpenger
import no.nav.dagpenger.regel.Søknadstidspunkt
import no.nav.dagpenger.regel.fastsetting.DagpengenesStørrelse
import no.nav.dagpenger.regel.fastsetting.Egenandel

class EgenandelSteg : No {
    private val fraDato = 10.mai(2024)
    private val regelsett =
        RegelverkDagpenger.regelsettFor(Egenandel.egenandel)
    private val opplysninger: Opplysninger = Opplysninger()
    private lateinit var regelkjøring: Regelkjøring

    @BeforeStep
    fun kjørRegler() {
        regelkjøring = Regelkjøring(fraDato, opplysninger, *regelsett.toTypedArray())
    }

    init {

        Gitt("at sats er {string}") { sats: String ->
            opplysninger.leggTil(
                Faktum(
                    Søknadstidspunkt.søknadstidspunkt,
                    fraDato,
                ),
            )
            opplysninger.leggTil(
                Faktum(
                    DagpengenesStørrelse.avrundetDagsMedBarnetillegg,
                    Beløp(sats.toBigDecimal()),
                ),
            )
        }

        Så("skal egenandel være {string}") { string: String ->
            opplysninger.finnOpplysning(Egenandel.egenandel).verdi shouldBe Beløp(string.toBigDecimal())
        }
    }
}
