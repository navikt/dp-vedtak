package no.nav.dagpenger.vedtak.iverksetting.mediator.mottak

import io.kotest.assertions.assertSoftly
import io.kotest.matchers.shouldBe
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import no.nav.dagpenger.vedtak.iverksetting.IverksettingsVedtak
import no.nav.dagpenger.vedtak.iverksetting.hendelser.VedtakFattetHendelse
import no.nav.dagpenger.vedtak.iverksetting.mediator.IverksettingMediator
import no.nav.dagpenger.vedtak.iverksetting.mediator.fattetVedtakJsonHendelse
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

class VedtakFattetMottakTest {

    private val testRapid = TestRapid()
    private val iverksettingMediatorMock = mockk<IverksettingMediator>()
    private val vedtakFattetMottak = VedtakFattetMottak(testRapid, iverksettingMediatorMock)

    @Test
    fun `Skal lese vedtakfattet hendelser `() {
        val vedtakFattetHendelse = slot<VedtakFattetHendelse>()
        every { iverksettingMediatorMock.håndter(capture(vedtakFattetHendelse)) } just Runs
        val fattetvedtakJson = fattetVedtakJsonHendelse()
        testRapid.sendTestMessage(fattetvedtakJson)

        verify(exactly = 1) {
            iverksettingMediatorMock.håndter(any())
        }

        assertSoftly {
            vedtakFattetHendelse.isCaptured shouldBe true
            val captured = vedtakFattetHendelse.captured
            captured.ident() shouldBe "string"
            captured.iverksettingsVedtak.vedtakId shouldBe UUID.fromString("df5e6587-a3e3-407c-8202-02f9740a09b0")
            captured.iverksettingsVedtak.behandlingId shouldBe UUID.fromString("0AAA66B9-35C2-4398-ACA0-D1D0A9465292")
            captured.iverksettingsVedtak.virkningsdato shouldBe LocalDate.of(2019, 8, 24)
            captured.iverksettingsVedtak.vedtakstidspunkt shouldBe LocalDateTime.parse("2019-08-24T14:15:22")
            captured.iverksettingsVedtak.utfall shouldBe IverksettingsVedtak.Utfall.Innvilget
        }
    }
}
