package no.nav.dagpenger.vedtak.mediator

import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import no.nav.dagpenger.vedtak.db.InMemoryMeldingRepository
import no.nav.dagpenger.vedtak.iverksetting.mediator.IverksettingMediator
import no.nav.dagpenger.vedtak.mediator.Meldingsfabrikk.dagpengerInnvilgetJson
import no.nav.dagpenger.vedtak.modell.hendelser.SøknadBehandletHendelse
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.util.UUID

internal class HendelseMessageMediatorTest {

    private val meldingSlot = slot<SøknadBehandletHendelse>()
    private val testRapid = TestRapid()
    private val personMediatorMock = mockk<PersonMediator>(relaxed = false)
    private val meldingRepository = InMemoryMeldingRepository()
    private val hendelseMediator = HendelseMediator(
        rapidsConnection = testRapid,
        hendelseRepository = meldingRepository,
        personMediator = personMediatorMock,
        iverksettingMediator = IverksettingMediator(mockk(), mockk(), mockk()),
    )

    @Test
    fun `Ta imot melding om innvilgelse, lagre og behandle`() {
        val meldingId = UUID.randomUUID()
        every { personMediatorMock.håndter(capture(meldingSlot)) } just Runs
        testRapid.sendTestMessage(dagpengerInnvilgetJson(meldingId = meldingId))
        assertTrue(meldingSlot.isCaptured)
        assertEquals(true, meldingRepository.erBehandlet(meldingId))
    }

    @Test
    fun `Ta i mot melding som feiler under behandling`() {
        val meldingId = UUID.randomUUID()
        every { personMediatorMock.håndter(any<SøknadBehandletHendelse>()) } throws RuntimeException("Feilet behandling")
        assertThrows<RuntimeException> { testRapid.sendTestMessage(dagpengerInnvilgetJson(meldingId = meldingId)) }
        assertEquals(false, meldingRepository.erBehandlet(meldingId))
    }
}
