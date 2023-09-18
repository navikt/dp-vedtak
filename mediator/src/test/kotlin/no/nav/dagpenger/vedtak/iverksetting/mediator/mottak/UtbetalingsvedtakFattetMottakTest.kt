package no.nav.dagpenger.vedtak.iverksetting.mediator.mottak

import io.kotest.assertions.assertSoftly
import io.kotest.matchers.shouldBe
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import no.nav.dagpenger.vedtak.iverksetting.hendelser.UtbetalingsvedtakFattetHendelse
import no.nav.dagpenger.vedtak.iverksetting.mediator.utbetalingsvedtakFattet
import no.nav.dagpenger.vedtak.juni
import no.nav.dagpenger.vedtak.mediator.IHendelseMediator
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.util.UUID

internal class UtbetalingsvedtakFattetMottakTest {

    private val testRapid = TestRapid()
    private val iHendelseMediator = mockk<IHendelseMediator>()
    private val vedtakId = UUID.fromString("df5e6587-a3e3-407c-8202-02f9740a09b0")
    private val behandlingId = UUID.fromString("0AAA66B9-35C2-4398-ACA0-D1D0A9465292")
    private val sakId = "SAK_NUMMER_1"
    private val ident = "12345678910"

    init {
        UtbetalingsvedtakFattetMottak(testRapid, iHendelseMediator)
    }

    @Test
    fun `Skal lese UtbetalingsvedtakFattet hendelser`() {
        val utbetalingVedtakFattetSlot = slot<UtbetalingsvedtakFattetHendelse>()
        every { iHendelseMediator.behandle(capture(utbetalingVedtakFattetSlot), any(), any()) } just Runs
        val utbetalingVedtakJson = utbetalingsvedtakFattet(
            ident = ident,
            vedtakId = vedtakId,
            behandlingId = behandlingId,
            sakId = sakId,
        )
        testRapid.sendTestMessage(utbetalingVedtakJson)

        verify(exactly = 1) {
            iHendelseMediator.behandle(any<UtbetalingsvedtakFattetHendelse>(), any(), any())
        }

        assertSoftly {
            utbetalingVedtakFattetSlot.isCaptured shouldBe true
            val utbetalingsvedtakFattet = utbetalingVedtakFattetSlot.captured
            utbetalingsvedtakFattet.ident() shouldBe ident
            utbetalingsvedtakFattet.vedtakId shouldBe vedtakId
            utbetalingsvedtakFattet.behandlingId shouldBe behandlingId
            utbetalingsvedtakFattet.sakId shouldBe sakId
            utbetalingsvedtakFattet.virkningsdato shouldBe (11 juni 2023)
            utbetalingsvedtakFattet.vedtakstidspunkt shouldBe LocalDateTime.MAX
            utbetalingsvedtakFattet.utbetalingsdager.size shouldBe 10
            utbetalingsvedtakFattet.utfall shouldBe UtbetalingsvedtakFattetHendelse.Utfall.Innvilget
        }
    }
}
