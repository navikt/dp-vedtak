package no.nav.dagpenger.features

import io.cucumber.java.BeforeStep
import io.cucumber.java8.No
import io.kotest.matchers.shouldBe
import no.nav.dagpenger.dato.mai
import no.nav.dagpenger.opplysning.Faktum
import no.nav.dagpenger.opplysning.Opplysning
import no.nav.dagpenger.opplysning.Opplysninger
import no.nav.dagpenger.opplysning.Regelkjøring
import no.nav.dagpenger.regel.RegelverkDagpenger
import no.nav.dagpenger.regel.SamordingUtenforFolketrygden.andreYtelser
import no.nav.dagpenger.regel.SamordingUtenforFolketrygden.andreØkonomiskeYtelser
import no.nav.dagpenger.regel.SamordingUtenforFolketrygden.skalSamordnesUtenforFolketrygden
import no.nav.dagpenger.regel.Søknadstidspunkt

class SamordningUtenforFolketrygdenSteg : No {
    private val fraDato = 10.mai(2022)
    private val regelsett =
        RegelverkDagpenger.regelsettFor(
            skalSamordnesUtenforFolketrygden,
        )
    private val opplysninger: Opplysninger = Opplysninger()
    private lateinit var regelkjøring: Regelkjøring

    @BeforeStep
    fun kjørRegler() {
        regelkjøring = Regelkjøring(fraDato, opplysninger, *regelsett.toTypedArray())
    }

    init {
        Gitt("at søker har søkt om dagpenger med andre ytelser") {
            opplysninger
                .leggTil(
                    Faktum(Søknadstidspunkt.søknadsdato, 11.mai(2022)) as Opplysning<*>,
                ).also { regelkjøring.evaluer() }
            opplysninger
                .leggTil(
                    Faktum(Søknadstidspunkt.ønsketdato, 11.mai(2022)) as Opplysning<*>,
                ).also { regelkjøring.evaluer() }
        }

        Gitt("søker har oppgitt ytelse {string}") { ytelse: String ->
            opplysninger.leggTil(Faktum(andreØkonomiskeYtelser, false))
            when (ytelse) {
                else -> opplysninger.leggTil(Faktum(andreYtelser, true)).also { regelkjøring.evaluer() }
            }
        }

        Så("skal vi kreve samordning") {
            opplysninger.finnOpplysning(skalSamordnesUtenforFolketrygden).verdi shouldBe true
        }
    }
}
