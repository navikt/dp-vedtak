package no.nav.dagpenger.vedtak.iverksetting.mediator.mottak

import io.kotest.assertions.assertSoftly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import no.nav.dagpenger.vedtak.august
import no.nav.dagpenger.vedtak.iverksetting.hendelser.DagpengerAvslått
import no.nav.dagpenger.vedtak.iverksetting.hendelser.DagpengerInnvilget
import no.nav.dagpenger.vedtak.iverksetting.hendelser.UtbetalingsvedtakFattetHendelse
import no.nav.dagpenger.vedtak.iverksetting.mediator.dagpengerAvslåttHendelse
import no.nav.dagpenger.vedtak.iverksetting.mediator.dagpengerInnvilgetHendelse
import no.nav.dagpenger.vedtak.iverksetting.mediator.førsteUtbetalingsvedtakFattet
import no.nav.dagpenger.vedtak.iverksetting.mediator.utbetalingsvedtakFattetNårDetFinnesTidligereUtbetalinger
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
        DagpengerInnvilgetMottak(testRapid, iHendelseMediator)
        DagpengerAvslåttMottak(testRapid, iHendelseMediator)
    }

    @Test
    fun `Skal lese DagpengerInnvilget hendelser`() {
        val vedtakFattetHendelseSlot = slot<DagpengerInnvilget>()
        every { iHendelseMediator.behandle(capture(vedtakFattetHendelseSlot), any(), any()) } just Runs
        val fattetvedtakJson = dagpengerInnvilgetHendelse(ident = ident, vedtakId = vedtakId, behandlingId = behandlingId, sakId = sakId)
        testRapid.sendTestMessage(fattetvedtakJson)

        verify(exactly = 1) {
            iHendelseMediator.behandle(any<DagpengerInnvilget>(), any(), any())
        }

        assertSoftly {
            vedtakFattetHendelseSlot.isCaptured shouldBe true
            val vedtakFattetHendelse = vedtakFattetHendelseSlot.captured
            vedtakFattetHendelse.ident() shouldBe ident
            vedtakFattetHendelse.vedtakId shouldBe vedtakId
            vedtakFattetHendelse.behandlingId shouldBe behandlingId
            vedtakFattetHendelse.sakId shouldBe sakId
            vedtakFattetHendelse.virkningsdato shouldBe (24 august 2019)
            vedtakFattetHendelse.vedtakstidspunkt shouldBe LocalDateTime.MAX
        }
    }

    @Test
    fun `Skal lese DagpengerAvslått hendelser`() {
        val dagpengerAvslåttSlot = slot<DagpengerAvslått>()
        every { iHendelseMediator.behandle(capture(dagpengerAvslåttSlot), any(), any()) } just Runs
        val fattetvedtakJson = dagpengerAvslåttHendelse(ident = ident, vedtakId = vedtakId, behandlingId = behandlingId, sakId = sakId)
        testRapid.sendTestMessage(fattetvedtakJson)

        verify(exactly = 1) {
            iHendelseMediator.behandle(any<DagpengerAvslått>(), any(), any())
        }

        assertSoftly {
            dagpengerAvslåttSlot.isCaptured shouldBe true
            val vedtakFattetHendelse = dagpengerAvslåttSlot.captured
            vedtakFattetHendelse.ident() shouldBe ident
            vedtakFattetHendelse.vedtakId shouldBe vedtakId
            vedtakFattetHendelse.behandlingId shouldBe behandlingId
            vedtakFattetHendelse.sakId shouldBe sakId
            vedtakFattetHendelse.virkningsdato shouldBe (24 august 2019)
            vedtakFattetHendelse.vedtakstidspunkt shouldBe LocalDateTime.MAX
        }
    }

    @Test
    fun `Skal lese UtbetalingsvedtakFattet hendelser - førstegangsutbetaling`() {
        val utbetalingVedtakFattetSlot = slot<UtbetalingsvedtakFattetHendelse>()
        every { iHendelseMediator.behandle(capture(utbetalingVedtakFattetSlot), any(), any()) } just Runs
        val utbetalingVedtakJson = førsteUtbetalingsvedtakFattet(ident = ident, vedtakId = vedtakId, behandlingId = behandlingId, sakId = sakId)
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
            utbetalingsvedtakFattet.forrigeBehandlingId shouldBe null
        }
    }

    @Test
    fun `Skal lese UtbetalingsvedtakFattet hendelser - ikke førstegangsutbetaling`() {
        val utbetalingVedtakFattetSlot = slot<UtbetalingsvedtakFattetHendelse>()
        every { iHendelseMediator.behandle(capture(utbetalingVedtakFattetSlot), any(), any()) } just Runs
        val utbetalingVedtakJson = utbetalingsvedtakFattetNårDetFinnesTidligereUtbetalinger(
            ident = ident,
            vedtakId = vedtakId,
            behandlingId = behandlingId,
            sakId = sakId,
            forrigeBehandlingId = UUID.randomUUID(),
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
            utbetalingsvedtakFattet.forrigeBehandlingId shouldNotBe null
        }
    }
}
