package no.nav.dagpenger.vedtak.mediator.mottak

import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.util.UUID

class SøknadBehandletMottakTest {

    private val testRapid = TestRapid().also {
        SøknadBehandletMottak(it)
    }

    @BeforeEach
    fun setUp() {
        testRapid.reset()
    }

    @Test
    fun `motta søknad innvilget hendelse`() {
        testRapid.sendTestMessage(søknadInnvilgetJson())
    }
}

fun søknadInnvilgetJson() =
    //language=JSON
    """
        {        
          "@event_name": "søknad_innvilget_hendelse",
          "behandlingId": "${UUID.randomUUID()}",
          "virkningsdato": "${LocalDate.now()}",
          "dagpengerettighet": "Ordinær",
          "dagsats": "500",
          "grunnlag": "500000",
          "stønadsperiode": "52",
          "vanligArbeidstidPerDag": "8",
          "antallVentedager": "3",
          "identer" : [ 
            {
              "id" : "12345123451",
              "type" : "folkeregisterident",
              "historisk" : false
            }, 
            {
              "id" : "aktørId",
              "type" : "aktørid",
              "historisk" : false
            } 
          ]
        } 
    """.trimIndent()
