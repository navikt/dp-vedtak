package no.nav.dagpenger.vedtak.cucumber

import io.cucumber.datatable.DataTable
import io.cucumber.java8.No
import no.nav.dagpenger.vedtak.modell.Person
import no.nav.dagpenger.vedtak.modell.PersonIdentifikator.Companion.tilPersonIdentfikator
import no.nav.dagpenger.vedtak.modell.hendelser.RapportertDag
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class NyRettighetTest() : No {

    companion object {
        val datoformatterer = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    }

    init {

        lateinit var person: Person

        Gitt("at bruker er gitt dagpenger") { vedtakshendelse: Vedtakshendelse ->

            person = Person(vedtakshendelse.fødselsnummer.tilPersonIdentfikator())
            // person.håndter(NyRettighetHendelse())
        }
        Så("har bruker vedtak i vedtakhistorikken") {
            // assertTrue(person.harVedtak())
        }
        Når("bruker rapporterer om dager") { dager: DataTable ->
            val rapporteringsdager = dager.column(0).map { RapportertDag(LocalDate.parse(it, datoformatterer)) }
            // person.håndter(RapporteringHendelse(rapporteringsdager))
        }
        Så("skal bruker få utbetalt for dager hen har jobbet") {
            // assertFalse(person.utbetalingsdager().isEmpty())
        }
    }

    private data class Vedtakshendelse(
        val fødselsnummer: String,
        val behandlingId: String,
    )
}
