package no.nav.dagpenger.features

import io.kotest.matchers.equals.shouldBeEqual
import no.nav.dagpenger.dato.mai
import no.nav.dagpenger.opplysning.Faktum
import no.nav.dagpenger.opplysning.Opplysninger
import no.nav.dagpenger.opplysning.Regelkjøring
import no.nav.dagpenger.regel.Opptjeningstid
import no.nav.dagpenger.regel.Søknadstidspunkt
import java.time.LocalDate

class OpptjeningstidSteg : RegelTest {
    private val fraDato = 10.mai(2022).atStartOfDay()
    override val regelsett = listOf(Opptjeningstid.regelsett, Søknadstidspunkt.regelsett)
    override val opplysninger = Opplysninger()

    init {

        Regelkjøring(fraDato, opplysninger, *regelsett.toTypedArray())

        Gitt(
            "at søknadstidspunktet er {dato}",
        ) { søknadstidspunktet: LocalDate ->
            opplysninger.leggTil(
                Faktum(
                    Søknadstidspunkt.søknadsdato,
                    søknadstidspunktet,
                ),
            )
            opplysninger.leggTil(
                Faktum(
                    Søknadstidspunkt.ønsketdato,
                    søknadstidspunktet,
                ),
            )
        }
        Så(
            "er arbeidsgivers pliktige rapporteringsfrist {dato}",
        ) { rapporteringsfrist: LocalDate ->
            val faktiskRapporteringsfrist = opplysninger.finnOpplysning(Opptjeningstid.justertRapporteringsfrist)
            rapporteringsfrist shouldBeEqual faktiskRapporteringsfrist.verdi
        }
        Så(
            "opptjeningstiden er fra {dato}",
        ) { sisteAvsluttendeDato: LocalDate ->
            val faktiskSisteAvsluttendeDato = opplysninger.finnOpplysning(Opptjeningstid.sisteAvsluttendendeKalenderMåned)
            sisteAvsluttendeDato shouldBeEqual faktiskSisteAvsluttendeDato.verdi
        }
    }
}
