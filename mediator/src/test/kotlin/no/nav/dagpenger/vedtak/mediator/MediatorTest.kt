import io.kotest.matchers.shouldBe
import no.nav.dagpenger.vedtak.mediator.Meldingsfabrikk.søknadInnvilgetJson
import no.nav.dagpenger.vedtak.mediator.PersonMediator
import no.nav.dagpenger.vedtak.mediator.persistens.InMemoryPersonRepository
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class MediatorTest {

    private val testRapid = TestRapid()
    private val personMediator = PersonMediator(testRapid, InMemoryPersonRepository())

    @BeforeEach
    fun setUp() {
        testRapid.reset()
    }

    @Test
    fun `Skal kunne opprette avtale når vi mottar melding om prosessresultat`() {
        testRapid.sendTestMessage(søknadInnvilgetJson())
        testRapid.inspektør.size shouldBe 1
        testRapid.inspektør.message(testRapid.inspektør.size - 1).also {
            assertEquals("vedtak_opprettet", it["@event_name"].asText())
        }
    }
}
