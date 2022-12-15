package no.nav.dagpenger.vedtak

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.cucumber.datatable.DataTable
import io.cucumber.java8.No
import no.nav.dagpenger.vedtak.modell.Person
import no.nav.dagpenger.vedtak.modell.PersonIdentifikator.Companion.tilPersonIdentfikator
import no.nav.dagpenger.vedtak.modell.hendelser.NyRettighetHendelse
import no.nav.dagpenger.vedtak.modell.hendelser.RapporteringHendelse
import no.nav.dagpenger.vedtak.modell.hendelser.RapportertDag
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import java.lang.reflect.Type
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class CucumberStegDefinisjoner() : No {

    companion object {
        private val objectMapper = jacksonObjectMapper()
            .registerModule(JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
        val datoformatterer = DateTimeFormatter.ofPattern("dd.MM.yyyy")
    }

    init {

        lateinit var person: Person

        Gitt("at bruker er gitt dagpenger") { vedtakshendelse: Vedtakshendelse ->

            person = Person(vedtakshendelse.fødselsnummer.tilPersonIdentfikator())
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

    private data class Vedtakshendelse(
        val fødselsnummer: String,
        val behandlingId: String,
    )

    init {
        DefaultParameterTransformer { fromValue: String?, toValueType: Type? ->
            objectMapper.convertValue(
                fromValue,
                objectMapper.constructType(toValueType)
            )
        }
        DefaultDataTableCellTransformer { fromValue: String?, toValueType: Type? ->
            objectMapper.convertValue(
                fromValue,
                objectMapper.constructType(toValueType)
            )
        }
        DefaultDataTableEntryTransformer { fromValue: Map<String?, String?>?, toValueType: Type? ->
            objectMapper.convertValue(
                fromValue,
                objectMapper.constructType(toValueType)
            )
        }
    }
}
