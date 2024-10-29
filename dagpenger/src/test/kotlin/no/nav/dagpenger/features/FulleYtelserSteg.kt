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
import no.nav.dagpenger.regel.Søknadstidspunkt.prøvingsdato

class FulleYtelserSteg : No {
    private val fraDato = 23.mai(2024)
    private val regelsett = listOf(FulleYtelser.regelsett, Søknadstidspunkt.regelsett)
    private val opplysninger = Opplysninger()
    private lateinit var regelkjøring: Regelkjøring

    @BeforeStep
    fun kjørRegler() {
        regelkjøring = Regelkjøring(fraDato, opplysninger, *regelsett.toTypedArray())
    }

    init {
        Gitt("har oppgitt i søknaden at søker {boolsk} andre ytelser") { andreYtelser: Boolean ->
            opplysninger.leggTil(Faktum(prøvingsdato, fraDato)).also { regelkjøring.evaluer() }
            opplysninger.leggTil(Faktum(FulleYtelser.andreYtelser, andreYtelser) as Opplysning<*>).also { regelkjøring.evaluer() }
        }
        Gitt("saksbehandler er {boolsk} i at brukeren har andre ytelser") { enig: Boolean ->
            opplysninger.leggTil(Faktum(FulleYtelser.vurderingAndreYtelser, enig) as Opplysning<*>).also { regelkjøring.evaluer() }
        }
        Gitt("ikke {boolsk} NAV-ytelser") { navYtelser: Boolean ->
            opplysninger.leggTil(Faktum(FulleYtelser.navYtelser, navYtelser) as Opplysning<*>).also { regelkjøring.evaluer() }
        }
        Så("skal søker få {boolsk} om ikke fulle ytelser") { utfall: Boolean ->
            val faktum = opplysninger.finnOpplysning(FulleYtelser.ikkeFulleYtelser)
            faktum.verdi shouldBe utfall
        }
    }
}
