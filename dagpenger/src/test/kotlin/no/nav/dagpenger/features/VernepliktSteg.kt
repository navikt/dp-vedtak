package no.nav.dagpenger.features

import io.cucumber.java.BeforeStep
import io.cucumber.java8.No
import io.kotest.matchers.shouldBe
import no.nav.dagpenger.dato.mai
import no.nav.dagpenger.opplysning.Faktum
import no.nav.dagpenger.opplysning.Opplysning
import no.nav.dagpenger.opplysning.Opplysninger
import no.nav.dagpenger.opplysning.Regelkjøring
import no.nav.dagpenger.regel.Verneplikt

class VernepliktSteg : No {
    private val fraDato = 23.mai(2024)
    private val regelsett = listOf(Verneplikt.regelsett)
    private val opplysninger = Opplysninger()
    private lateinit var regelkjøring: Regelkjøring

    @BeforeStep
    fun kjørRegler() {
        regelkjøring = Regelkjøring(fraDato, opplysninger, *regelsett.toTypedArray())
    }

    init {
        Gitt("at søker har {boolsk} om dagpenger under verneplikt") { søkt: Boolean ->
            opplysninger.leggTil(Faktum<Boolean>(Verneplikt.avtjentVerneplikt, søkt) as Opplysning<*>)
        }

        Gitt("saksbehandler vurderer at søker har {boolsk} kravet til verneplikt") { oppfyller: Boolean ->
            opplysninger.leggTil(Faktum<Boolean>(Verneplikt.vurderingAvVerneplikt, oppfyller) as Opplysning<*>)
        }

        Så("skal søker få {boolsk} av verneplikt") { utfall: Boolean ->
            val faktum = opplysninger.finnOpplysning(Verneplikt.vurderingAvVerneplikt)
            faktum.verdi shouldBe utfall
        }
    }
}
