package no.nav.dagpenger.vedtak.iverksetting.mediator

import io.kotest.assertions.assertSoftly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.mockk
import mu.KotlinLogging
import no.nav.dagpenger.vedtak.db.InMemoryMeldingRepository
import no.nav.dagpenger.vedtak.mediator.BehovMediator
import no.nav.dagpenger.vedtak.mediator.HendelseMediator
import no.nav.dagpenger.vedtak.mediator.Meldingsfabrikk.behovOmIverksettingAvUtbetalingsvedtak
import no.nav.dagpenger.vedtak.mediator.PersonMediator
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID
import java.util.UUID.randomUUID

internal class IverksettingMediatorTest {

    private val testRapid = TestRapid()
    private val vedtakId = UUID.fromString("408F11D9-4BE8-450A-8B7A-C2F3F9811859")
    private val ident = "12345123451"
    private val sakId = "SAK_NUMMER_1"
    private val iverksettingRepository = InMemoryIverksettingRepository()

    init {
        HendelseMediator(
            rapidsConnection = testRapid,
            hendelseRepository = InMemoryMeldingRepository(),
            personMediator = PersonMediator(mockk(), mockk()),
            iverksettingMediator = IverksettingMediator(
                aktivitetsloggMediator = mockk(relaxed = true),
                iverksettingRepository = iverksettingRepository,
                behovMediator = BehovMediator(testRapid, KotlinLogging.logger {}),
            ),
        )
    }

    @BeforeEach
    fun setUp() {
        testRapid.reset()
    }

    @Test
    fun `Vedtakfattet hendelse fører til en iverksettelse og behov om iverksetting`() {
        testRapid.sendTestMessage(dagpengerInnvilgetHendelse(vedtakId, behandlingId = randomUUID(), ident, sakId))
        assertSoftly {
            testRapid.inspektør.size shouldBe 1
            val rammevedtakJson = testRapid.inspektør.message(0)
            rammevedtakJson["@event_name"].asText() shouldBe "behov"
            rammevedtakJson["@behov"].map { it.asText() } shouldBe listOf("Iverksett")
        }

        testRapid.sendTestMessage(behovOmIverksettingAvUtbetalingsvedtak(vedtakId))

        iverksettingRepository.hent(vedtakId) shouldNotBe null
    }
}
