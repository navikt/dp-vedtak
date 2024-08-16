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
import no.nav.dagpenger.regel.fastsetting.Dagpengegrunnlag
import no.nav.dagpenger.regel.fastsetting.DagpengensStørrelse.dagpengensStørrelse

class DagpengensStørrelseSteg : No {
    private val fraDato = 10.mai(2021)
    private val regelsett = RegelverkDagpenger.regelsettFor(dagpengensStørrelse)
    private val opplysninger: Opplysninger = Opplysninger()
    private lateinit var regelkjøring: Regelkjøring

    @BeforeStep
    fun kjørRegler() {
        regelkjøring = Regelkjøring(fraDato, opplysninger, *regelsett.toTypedArray())
    }

    init {
        Gitt("at dagpengegrunnlag er {string}") { grunnlag: String ->
            regelkjøring.leggTil(
                Faktum(
                    Søknadstidspunkt.søknadstidspunkt,
                    fraDato,
                ),
            )
            regelkjøring.leggTil(
                Faktum(
                    Dagpengegrunnlag.grunnlag,
                    Beløp(grunnlag.toBigDecimal()),
                ),
            )
        }

        Og("at søker har ikke barn") {
            // Vi har ikke barn
        }
        Så("skal dagpengens størrelse være {string}") { størrelse: String ->
            opplysninger.finnOpplysning(dagpengensStørrelse).verdi shouldBe Beløp(størrelse.toBigDecimal())
        }
    }
}
