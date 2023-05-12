package no.nav.dagpenger.vedtak.iverksetting.mediator.behovløsere

import io.kotest.matchers.shouldBe
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import no.nav.dagpenger.vedtak.iverksetting.mediator.behovløsere.models.IverksettDagpengerdDto
import no.nav.dagpenger.vedtak.mediator.Meldingsfabrikk.iverksettJson
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.junit.jupiter.api.Test

internal class IverksettBehovløserTest {

    private val testRapid = TestRapid()
    private val iverksettClient = mockk<IverksettClient>()
    init {
        IverksettBehovløser(testRapid, iverksettClient)
    }

    @Test
    fun `at vi kaller iverksett APIet og besvarer behovet 'Iverksett'`() {
        val iverksettDtoSlot = slot<IverksettDagpengerdDto>()
        coEvery { iverksettClient.iverksett(capture(iverksettDtoSlot)) } just Runs

        testRapid.sendTestMessage(iverksettJson())
        coVerify(exactly = 1) {
            iverksettClient.iverksett(any())
        }
        iverksettDtoSlot.isCaptured shouldBe true
        testRapid.inspektør.size shouldBe 1
        with(testRapid.inspektør.message(0)) {
            val løsning = this["@løsning"]["Iverksett"]
            løsning.asBoolean() shouldBe true
        }
    }
}
