package no.nav.dagpenger.vedtak.iverksetting.mediator

import io.kotest.assertions.assertSoftly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.mockk
import mu.KotlinLogging
import no.nav.dagpenger.vedtak.mediator.BehovMediator
import no.nav.dagpenger.vedtak.mediator.HendelseMediator
import no.nav.dagpenger.vedtak.mediator.Meldingsfabrikk.iverksettJson
import no.nav.dagpenger.vedtak.mediator.PersonMediator
import no.nav.dagpenger.vedtak.mediator.persistens.InMemoryMeldingRepository
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID

internal class IverksettingMediatorTest {

    private val testRapid = TestRapid()
    private val vedtakId = UUID.fromString("408F11D9-4BE8-450A-8B7A-C2F3F9811859")

    private val iverksettingRepository = InMemoryIverksettingRepository()

    init {
        HendelseMediator(
            rapidsConnection = testRapid,
            hendelseRepository = InMemoryMeldingRepository(),
            personMediator = PersonMediator(mockk(), mockk()),
            iverksettingMediator = IverksettingMediator(
                iverksettingRepository,
                BehovMediator(testRapid, KotlinLogging.logger {}),
            ),
        )
    }

    @BeforeEach
    fun setUp() {
        testRapid.reset()
    }

    @Test
    fun `Vedtakfattet hendelse fører til en iverksettelse og behov om iverksetting`() {
        testRapid.sendTestMessage(fattetVedtakJsonHendelse(vedtakId))
        assertSoftly {
            testRapid.inspektør.size shouldBe 1
            val message = testRapid.inspektør.message(0)
            message["@event_name"].asText() shouldBe "behov"
            message["@behov"].map { it.asText() } shouldBe listOf("Iverksett")
        }
        testRapid.sendTestMessage(
            iverksettJson(vedtakId),
        )

        iverksettingRepository.hent(vedtakId) shouldNotBe null
    }
}
