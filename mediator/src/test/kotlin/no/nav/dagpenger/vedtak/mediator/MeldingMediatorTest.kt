package no.nav.dagpenger.vedtak.mediator

import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import no.nav.dagpenger.vedtak.mediator.Meldingsfabrikk.dagpengerInnvilgetJson
import no.nav.dagpenger.vedtak.mediator.persistens.InMemoryMeldingRepository
import no.nav.dagpenger.vedtak.modell.hendelser.SøknadBehandletHendelse
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class MeldingMediatorTest {

    private val meldingSlot = slot<SøknadBehandletHendelse>()
    private val testRapid = TestRapid()
    private val personMediatorMock = mockk<PersonMediator>(relaxed = false)
    private val meldingRepository = InMemoryMeldingRepository()
    private val meldingMediator = MeldingMediator(
        rapidsConnection = testRapid,
        meldingRepository = meldingRepository,
        personMediator = personMediatorMock,
    )

    @Test
    fun `Ta imot melding om innvilgelse, lagre og behandle`() {
        every { personMediatorMock.håndter(capture(meldingSlot)) } just Runs
        testRapid.sendTestMessage(dagpengerInnvilgetJson())
        assertTrue(meldingSlot.isCaptured)
        assertEquals(1, meldingRepository.hentBehandlede().size)
    }

    @Test
    fun `Ta i mot melding som feiler under behandling`() {
        every { personMediatorMock.håndter(any()) } throws RuntimeException("Feilet behandling")
        assertThrows<RuntimeException> { testRapid.sendTestMessage(dagpengerInnvilgetJson()) }
        assertEquals(0, meldingRepository.hentBehandlede().size)
        assertEquals(1, meldingRepository.hentFeilede().size)
    }
}
