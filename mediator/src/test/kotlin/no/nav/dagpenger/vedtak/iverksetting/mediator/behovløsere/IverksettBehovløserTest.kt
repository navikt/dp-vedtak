package no.nav.dagpenger.vedtak.iverksetting.mediator.behovløsere

import io.kotest.matchers.shouldBe
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import no.nav.dagpenger.kontrakter.iverksett.IverksettDto
import no.nav.dagpenger.vedtak.mediator.Meldingsfabrikk.behovOmIverksettingAvLøpendeVedtak
import no.nav.dagpenger.vedtak.mediator.Meldingsfabrikk.behovOmIverksettingAvRammevedtak
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.junit.jupiter.api.Test

internal class IverksettBehovløserTest {

    private val testRapid = TestRapid()
    private val iverksettClient = mockk<IverksettClient>()

    init {
        IverksettBehovløser(testRapid, iverksettClient)
    }

    @Test
    fun `Motta rammevedtak, kall iverksett APIet og løs 'Iverksett' behovet`() {
        val iverksettDtoSlot = slot<IverksettDto>()
        coEvery { iverksettClient.iverksett(capture(iverksettDtoSlot)) } just Runs

        testRapid.sendTestMessage(behovOmIverksettingAvRammevedtak())
        coVerify(exactly = 1) {
            iverksettClient.iverksett(any())
        }
        iverksettDtoSlot.isCaptured shouldBe true
        testRapid.inspektør.size shouldBe 1
        val løstIverksettingJson = testRapid.inspektør.message(0)
        løstIverksettingJson["@løsning"]["Iverksett"].asBoolean() shouldBe true
    }

    @Test
    fun `Motta løpendeVedtak, kall iverksett APIet og løs 'Iverksett' behovet`() {
        val iverksettDtoSlot = slot<IverksettDto>()
        coEvery { iverksettClient.iverksett(capture(iverksettDtoSlot)) } just Runs

        testRapid.sendTestMessage(behovOmIverksettingAvLøpendeVedtak())
        coVerify(exactly = 1) {
            iverksettClient.iverksett(any())
        }
        iverksettDtoSlot.isCaptured shouldBe true
        testRapid.inspektør.size shouldBe 1
        val løstIverksettingJson = testRapid.inspektør.message(0)
        løstIverksettingJson["@løsning"]["Iverksett"].asBoolean() shouldBe true
    }
}
