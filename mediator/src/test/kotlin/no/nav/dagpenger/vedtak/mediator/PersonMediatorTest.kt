import io.kotest.matchers.shouldBe
import no.nav.dagpenger.vedtak.mediator.MeldingMediator
import no.nav.dagpenger.vedtak.mediator.Meldingsfabrikk.dagpengerAvslåttJson
import no.nav.dagpenger.vedtak.mediator.Meldingsfabrikk.dagpengerInnvilgetJson
import no.nav.dagpenger.vedtak.mediator.PersonMediator
import no.nav.dagpenger.vedtak.mediator.persistens.InMemoryMeldingRepository
import no.nav.dagpenger.vedtak.mediator.persistens.InMemoryPersonRepository
import no.nav.dagpenger.vedtak.mediator.vedtak.VedtakFattetKafkaObserver
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class PersonMediatorTest {

    private val testRapid = TestRapid()
    val personMediator = MeldingMediator(
        rapidsConnection = testRapid,
        meldingRepository = InMemoryMeldingRepository(),
        personMediator = PersonMediator(
            personRepository = InMemoryPersonRepository(),
            personObservers = listOf(VedtakFattetKafkaObserver(testRapid)),
        ),
    )

    @BeforeEach
    fun setUp() {
        testRapid.reset()
    }

    @Test
    fun `Innvilgelse av dagpenger hendelse fører til vedtak fattet event`() {
        testRapid.sendTestMessage(dagpengerInnvilgetJson())
        testRapid.inspektør.size shouldBe 1
        testRapid.inspektør.message(testRapid.inspektør.size - 1).also {
            assertEquals("vedtak_fattet", it["@event_name"].asText())
        }
    }

    @Test
    fun `Avslag av dagpenger hendelse fører til vedtak fattet event`() {
        testRapid.sendTestMessage(dagpengerAvslåttJson())
        testRapid.inspektør.size shouldBe 1
        testRapid.inspektør.message(testRapid.inspektør.size - 1).also {
            assertEquals("vedtak_fattet", it["@event_name"].asText())
        }
    }
}
