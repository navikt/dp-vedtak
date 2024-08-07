package no.nav.dagpenger.features

import io.cucumber.java.BeforeStep
import io.cucumber.java8.No
import io.kotest.matchers.shouldBe
import no.nav.dagpenger.dato.mai
import no.nav.dagpenger.opplysning.Faktum
import no.nav.dagpenger.opplysning.Opplysning
import no.nav.dagpenger.opplysning.Opplysninger
import no.nav.dagpenger.opplysning.Regelkjøring
import no.nav.dagpenger.regel.FulleYtelser
import no.nav.dagpenger.regel.Søknadstidspunkt
import java.time.LocalDate

class FulleYtelserSteg : No {
    private val fraDato = 23.mai(2024)
    private val regelsett = listOf(FulleYtelser.regelsett)
    private val opplysninger = Opplysninger()
    private lateinit var regelkjøring: Regelkjøring

    @BeforeStep
    fun kjørRegler() {
        regelkjøring = Regelkjøring(fraDato, opplysninger, *regelsett.toTypedArray())
    }

    init {

        Gitt("har oppgitt i søknaden at søker {boolsk} andre ytelser") { andreYtelser: Boolean ->
            regelkjøring.leggTil(Faktum<LocalDate>(Søknadstidspunkt.søknadstidspunkt, fraDato) as Opplysning<*>)
            regelkjøring.leggTil(Faktum<Boolean>(FulleYtelser.andreYtelser, andreYtelser) as Opplysning<*>)
        }
        Gitt("saksbehandler er {boolsk} i at brukeren har andre ytelser") { enig: Boolean ->
            regelkjøring.leggTil(Faktum<Boolean>(FulleYtelser.vurderingAndreYtelser, enig) as Opplysning<*>)
        }
        Gitt("ikke {boolsk} NAV-ytelser") { navYtelser: Boolean ->
            regelkjøring.leggTil(Faktum<Boolean>(FulleYtelser.navYtelser, navYtelser) as Opplysning<*>)
        }
        Så("skal søker få {boolsk} om ikke fulle ytelser") { utfall: Boolean ->
            val faktum = opplysninger.finnOpplysning(FulleYtelser.ikkeFulleYtelser)
            faktum.verdi shouldBe utfall
        }
    }
}
