package no.nav.dagpenger.vedtak.mediator.mottak

import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import no.nav.dagpenger.vedtak.mediator.IHendelseMediator
import no.nav.dagpenger.vedtak.mediator.Meldingsfabrikk.dagpengerAvslåttJson
import no.nav.dagpenger.vedtak.mediator.Meldingsfabrikk.dagpengerInnvilgetJson
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class SøknadBehandletMottakTest {
    private val hendelseMediatorMock = mockk<IHendelseMediator>().also {
        every { it.behandle(any(), any<SøknadBehandletHendelseMessage>(), any()) } just Runs
    }
    private val testRapid = TestRapid().also {
        SøknadBehandletMottak(it, hendelseMediatorMock)
    }

    @BeforeEach
    fun setUp() {
        testRapid.reset()
    }

    @Test
    fun `motta dagpenger innvilget hendelse`() {
        testRapid.sendTestMessage(dagpengerInnvilgetJson())
        verify(exactly = 1) {
            hendelseMediatorMock.behandle(any(), any<SøknadBehandletHendelseMessage>(), any())
        }
    }

    @Test
    fun `motta dagpenger avslått hendelse`() {
        testRapid.sendTestMessage(dagpengerAvslåttJson())
        verify(exactly = 1) {
            hendelseMediatorMock.behandle(any(), any<SøknadBehandletHendelseMessage>(), any())
        }
    }

    @Test
    fun `avslå meldinger som ikke validerer`() {
        testRapid.sendTestMessage(dagpengerInnvilgetJson(rettighetstype = "bla bla"))
        verify(exactly = 0) {
            hendelseMediatorMock.behandle(any(), any<SøknadBehandletHendelseMessage>(), any())
        }
    }
}
