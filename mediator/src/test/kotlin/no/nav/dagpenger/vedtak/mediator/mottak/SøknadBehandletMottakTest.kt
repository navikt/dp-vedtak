package no.nav.dagpenger.vedtak.mediator.mottak

import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import no.nav.dagpenger.vedtak.mediator.Meldingsfabrikk.dagpengerAvslåttJson
import no.nav.dagpenger.vedtak.mediator.Meldingsfabrikk.dagpengerInnvilgetJson
import no.nav.dagpenger.vedtak.mediator.MessageMediator
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class SøknadBehandletMottakTest {
    private val messageMediatorMock = mockk<MessageMediator>().also {
        every { it.håndter(any()) } just Runs
    }
    private val testRapid = TestRapid().also {
        SøknadBehandletMottak(it, messageMediatorMock)
    }

    @BeforeEach
    fun setUp() {
        testRapid.reset()
    }

    @Test
    fun `motta dagpenger innvilget hendelse`() {
        testRapid.sendTestMessage(dagpengerInnvilgetJson())
        verify(exactly = 1) {
            messageMediatorMock.håndter(any())
        }
    }

    @Test
    fun `motta dagpenger avslått hendelse`() {
        testRapid.sendTestMessage(dagpengerAvslåttJson())
        verify(exactly = 1) {
            messageMediatorMock.håndter(any())
        }
    }
}
