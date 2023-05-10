package no.nav.dagpenger.vedtak.iverksetting.mediator

import io.kotest.matchers.shouldBe
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import no.nav.dagpenger.vedtak.iverksetting.mediator.persistens.IverksettingRepository
import no.nav.dagpenger.vedtak.mediator.HendelseMediator
import no.nav.dagpenger.vedtak.mediator.PersonMediator
import no.nav.dagpenger.vedtak.mediator.persistens.InMemoryMeldingRepository
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import java.util.UUID

internal class IverksettingMediatorTest {

    private val testRapid = TestRapid()
    private val vedtakId = UUID.fromString("408F11D9-4BE8-450A-8B7A-C2F3F9811859")

    private val iverksettingRepository = mockk<IverksettingRepository>().also {
        every { it.hent(vedtakId) } returns null
        every { it.lagre(any()) } just Runs
    }

    val iverksettingMediator = HendelseMediator(
        rapidsConnection = testRapid,
        meldingRepository = InMemoryMeldingRepository(),
        personMediator = PersonMediator(mockk(), mockk()),
        iverksettingMediator = IverksettingMediator(iverksettingRepository),
    )

    @BeforeEach
    fun setUp() {
        testRapid.reset()
    }

    @Test
    fun `Vedtakfattet hendelse fører til en iverksettelse og behov om iverksetting`() {
        testRapid.sendTestMessage(fattetVedtakJsonHendelse())
        testRapid.inspektør.size shouldBe 1
    }
}
