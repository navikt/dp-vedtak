package no.nav.dagpenger.vedtak.mediator.mottak

import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import no.nav.dagpenger.vedtak.mediator.IHendelseMediator
import no.nav.dagpenger.vedtak.mediator.Meldingsfabrikk.rapporteringInnsendtJson
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class RapporteringBehandletMottakTest {
    private val hendelseMediatorMock =
        mockk<IHendelseMediator>().also {
            every { it.behandle(any(), any<RapporteringBehandletHendelseMessage>(), any()) } just Runs
        }

    private val testRapid =
        TestRapid().also {
            RapporteringBehandletMottak(it, hendelseMediatorMock)
        }

    @BeforeEach
    fun setUp() {
        testRapid.reset()
    }

    @Test
    fun `motta rapportering behandlet hendelse`() {
        testRapid.sendTestMessage(rapporteringInnsendtJson())
        verify(exactly = 1) {
            hendelseMediatorMock.behandle(any(), any<RapporteringBehandletHendelseMessage>(), any())
        }
    }
}
