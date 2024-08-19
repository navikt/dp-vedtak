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
import no.nav.dagpenger.regel.fastsetting.DagpengensStørrelse
import no.nav.dagpenger.regel.fastsetting.DagpengensStørrelse.antallBarn
import no.nav.dagpenger.regel.fastsetting.DagpengensStørrelse.avrundetDagsMedBarnetillegg
import no.nav.dagpenger.regel.fastsetting.DagpengensStørrelse.dagsatsUtenBarnetillegg
import no.nav.dagpenger.regel.fastsetting.Egenandel

class DagpengensStørrelseSteg : No {
    private val fraDato = 10.mai(2024)
    private val regelsett =
        listOf(DagpengensStørrelse.regelsett, Egenandel.regelsett)
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
            regelkjøring.leggTil(Faktum(antallBarn, 0))
        }

        Gitt("at søker har {int} barn") { antall: Int ->
            regelkjøring.leggTil(Faktum(antallBarn, antall))
        }

        Så("skal dagpengens uavrundet størrelse uten barnetillegg være {string}") { størrelse: String ->
            opplysninger.finnOpplysning(dagsatsUtenBarnetillegg).verdi shouldBe Beløp(størrelse.toBigDecimal())
        }

        Så("skal dagpengens størrelse være {string}") { størrelse: String ->
            opplysninger.finnOpplysning(avrundetDagsMedBarnetillegg).verdi shouldBe Beløp(størrelse.toBigDecimal())
        }

        Så("skal ukessats være {string}") { ukessats: String ->
            opplysninger.finnOpplysning(DagpengensStørrelse.ukessats).verdi shouldBe Beløp(ukessats.toBigDecimal())
        }
    }
}
