package no.nav.dagpenger.features

import io.cucumber.java8.No
import io.kotest.matchers.equals.shouldBeEqual
import no.nav.dagpenger.dato.mai
import no.nav.dagpenger.opplysning.Faktum
import no.nav.dagpenger.opplysning.Opplysninger
import no.nav.dagpenger.opplysning.Regelkjøring
import no.nav.dagpenger.regel.Opptjeningstid
import no.nav.dagpenger.regel.Søknadstidspunkt
import java.time.LocalDate

class OpptjeningstidSteg : No {
    private val forDato = 10.mai(2022)
    private val regelsett = listOf(Opptjeningstid.regelsett, Søknadstidspunkt.regelsett)
    private val opplysninger = Opplysninger(forDato = forDato)

    init {
        val regelkjøring = Regelkjøring(forDato, opplysninger, *regelsett.toTypedArray())

        Gitt(
            "at søknadstidspunktet er {dato}",
        ) { søknadstidspunktet: LocalDate ->
            regelkjøring.leggTil(
                Faktum(
                    Søknadstidspunkt.søknadsdato,
                    søknadstidspunktet,
                ),
            )
            regelkjøring.leggTil(
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
