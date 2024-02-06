package no.nav.dagpenger.behandling.features

import io.cucumber.java8.No
import no.nav.dagpenger.behandling.Faktum
import no.nav.dagpenger.behandling.Opplysninger
import no.nav.dagpenger.behandling.Regelkjøring
import no.nav.dagpenger.behandling.mai
import no.nav.dagpenger.behandling.regelsett.Alderskrav
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class AlderskravSteps : No {
    private val fraDato = 10.mai.atStartOfDay()
    private val opplysninger = Opplysninger()
    val regelkjøring = Regelkjøring(fraDato, opplysninger, Alderskrav.regelsett)

    init {
        Gitt("at fødselsdatoen til søkeren er {string}") { fødselsdato: String ->
            opplysninger.leggTil(Faktum(Alderskrav.fødselsdato, fødselsdato.somLocalDate()))
        }
        Gitt("at virkningstidspunktet er {string}") { virkningsdato: String ->
            opplysninger.leggTil(Faktum(Alderskrav.virkningsdato, virkningsdato.somLocalDate()))
        }
        Så("skal utfallet være {string}") { utfall: String ->
            val verdi =
                when (utfall) {
                    "Ja" -> true
                    "Nei" -> false
                    else -> throw IllegalArgumentException("Ukjent utfall: $utfall")
                }
            assertTrue(opplysninger.har(Alderskrav.vilkår))
            assertEquals(verdi, opplysninger.finnOpplysning(Alderskrav.vilkår).verdi)
        }
    }
}

private fun String.somLocalDate(): LocalDate {
    val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    return LocalDate.parse(this, formatter)
}
