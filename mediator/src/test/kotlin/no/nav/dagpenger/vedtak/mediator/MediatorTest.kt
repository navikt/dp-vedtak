import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.util.UUID

internal class MediatorTest {

    val testRapid = TestRapid()

    @BeforeEach
    fun setUp() {
        testRapid.reset()
    }

    @Test
    fun `Skal kunne opprette avtale når vi mottar melding om prosessresultat`() {

        testRapid.sendTestMessage(meldingOmInnvilget)
        testRapid.inspektør.message(testRapid.inspektør.size - 1).also {
            assertEquals("vedtak_opprettet", it["@event_name"].asText())
        }
    }

    //language=JSON
    private val meldingOmInnvilget = """
        {
        "@event_name": "prosess_resultat",
        "@opprettet": ${LocalDate.now()},
        "@id": ${UUID.randomUUID()},
        "søknad_uuid": ${UUID.randomUUID()},
        "resultat": true,
        "identer": [{
          "id": "123",
          "type": "folkeregisterident",
          "historisk": false
        }],
        "fakta": {},
        "subsumsjoner": {}
        }
    """.trimIndent()
}
