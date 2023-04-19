package no.nav.dagpenger.vedtak.mediator

import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.slot
import no.nav.dagpenger.vedtak.mediator.Meldingsfabrikk.dagpengerInnvilgetJson
import no.nav.dagpenger.vedtak.mediator.persistens.InMemoryMeldingRepository
import no.nav.dagpenger.vedtak.modell.hendelser.SøknadBehandletHendelse
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class MeldingMediatorTest {

    private val meldingSlot = slot<SøknadBehandletHendelse>()
    private val testRapid = TestRapid()
    private val personMediatorMock = mockk<PersonMediator>().also {
        every { it.håndter(capture(meldingSlot)) } just runs
    }
    private val meldingRepository = InMemoryMeldingRepository()
    private val meldingMediator = MeldingMediator(
        rapidsConnection = testRapid,
        meldingRepository = meldingRepository,
        personMediator = personMediatorMock,
    )

    @Test
    fun `Ta imot melding om innvilgelse og lagre`() {
        testRapid.sendTestMessage(dagpengerInnvilgetJson())
        assertTrue(meldingSlot.isCaptured)
        assertEquals(1, meldingRepository.hent().size)
    }
}
