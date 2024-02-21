package no.nav.dagpenger.features

import io.cucumber.java8.Scenario
import no.nav.dagpenger.dato.mai
import no.nav.dagpenger.features.utils.somLocalDate
import no.nav.dagpenger.opplysning.Faktum
import no.nav.dagpenger.opplysning.Opplysninger
import no.nav.dagpenger.opplysning.Regelkjøring
import no.nav.dagpenger.regel.Alderskrav
import no.nav.dagpenger.regel.Virkningsdato
import org.junit.jupiter.api.Assertions

class AlderskravSteg : RegelTest {
    private val fraDato = 10.mai(2022).atStartOfDay()
    override val regelsett = listOf(Alderskrav.regelsett, Virkningsdato.regelsett)
    override val opplysninger = Opplysninger()

    init {
        Regelkjøring(fraDato, opplysninger, *regelsett.toTypedArray())

        Gitt("at fødselsdatoen til søkeren er {string}") { fødselsdato: String ->
            opplysninger.leggTil(
                Faktum(
                    Alderskrav.fødselsdato,
                    fødselsdato.somLocalDate(),
                ),
            )
        }
        Gitt("at virkningstidspunktet er {string}") { virkningsdato: String ->
            opplysninger.leggTil(
                Faktum(
                    Virkningsdato.søknadsdato,
                    virkningsdato.somLocalDate(),
                ),
            )
        }
        Så("skal utfallet være {string}") { utfall: String ->
            val verdi =
                when (utfall) {
                    "Ja" -> true
                    "Nei" -> false
                    else -> throw IllegalArgumentException("Ukjent utfall: $utfall")
                }
            Assertions.assertTrue(opplysninger.har(Alderskrav.vilkår))
            Assertions.assertEquals(
                verdi,
                opplysninger.finnOpplysning(Alderskrav.vilkår).verdi,
            )
        }

        After { scenario: Scenario ->
            scenario.attach(skrivRegeltre(), "text/markdown", "regeltre.md")
        }
    }
}
