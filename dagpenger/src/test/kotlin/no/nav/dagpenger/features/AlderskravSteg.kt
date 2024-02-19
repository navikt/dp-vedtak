package no.nav.dagpenger.features

import io.cucumber.java8.No
import no.nav.dagpenger.dato.mai
import no.nav.dagpenger.opplysning.Faktum
import no.nav.dagpenger.opplysning.Opplysninger
import no.nav.dagpenger.opplysning.Regelkjøring
import no.nav.dagpenger.regel.Alderskrav
import no.nav.dagpenger.regel.Virkningsdato
import org.junit.jupiter.api.Assertions
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class AlderskravSteg : No {
    private val fraDato = 10.mai(2022).atStartOfDay()
    private val opplysninger = Opplysninger()
    val regelkjøring = Regelkjøring(fraDato, opplysninger, Alderskrav.regelsett, Virkningsdato.regelsett)

    init {
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
    }
}

private fun String.somLocalDate(): LocalDate {
    val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    return LocalDate.parse(this, formatter)
}
