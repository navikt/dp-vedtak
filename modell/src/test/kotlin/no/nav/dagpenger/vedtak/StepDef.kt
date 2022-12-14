package no.nav.dagpenger.vedtak

import io.cucumber.datatable.DataTable
import io.cucumber.java8.No
import no.nav.dagpenger.vedtak.modell.Person
import no.nav.dagpenger.vedtak.modell.PersonIdentifikator.Companion.tilPersonIdentfikator
import no.nav.dagpenger.vedtak.modell.hendelser.NyRettighetHendelse
import no.nav.dagpenger.vedtak.modell.hendelser.RapporteringHendelse
import no.nav.dagpenger.vedtak.modell.hendelser.RapportertDag
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class Steg : No {

    companion object {
        val datoformatterer = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    }
    init {

        lateinit var person: Person

        DataTableType { entry: Map<String, String> ->
            Vedtakshendelse(entry)
        }

        Gitt("at bruker er gitt dagpenger") { hendelse: DataTable ->
            val vedtakshendelse = hendelse.asList(Vedtakshendelse::class.java).first()
            person = Person(vedtakshendelse.person().tilPersonIdentfikator())
            person.håndter(NyRettighetHendelse())
        }
        Så("har bruker vedtak i vedtakhistorikken") {
            assertTrue(person.harVedtak())
        }
        Når("bruker rapporterer om dager") { dager: DataTable ->
            val rapporteringsdager = dager.column(0).map { RapportertDag(LocalDate.parse(it, datoformatterer)) }
            person.håndter(RapporteringHendelse(rapporteringsdager))
        }
        Så("skal bruker få utbetalt for dager hen har jobbet") {
            assertFalse(person.dagerTilBetaling().isEmpty())
        }
    }

    private data class Vedtakshendelse(private val verdier: Map<String, String>) {

        fun person(): String = verdier["Fødselsnummer"] ?: throw AssertionError("Må definere \"Fødselsnummer\"")
        fun behandlingsId(): String = verdier["BehandlingId"] ?: throw AssertionError("Må definere \"BehandlingId\"")
    }
}
