package no.nav.dagpenger.vedtak.mediator.mottak

import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import no.nav.dagpenger.vedtak.mediator.Meldingsfabrikk.søknadInnvilgetJson
import no.nav.dagpenger.vedtak.mediator.PersonMediator
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class SøknadBehandletMottakTest {
    private val personRepositoryMock = mockk<PersonMediator>().also {
        every { it.håndter(any()) } just Runs
    }
    private val testRapid = TestRapid().also {
        SøknadBehandletMottak(it, personRepositoryMock)
    }

    @BeforeEach
    fun setUp() {
        testRapid.reset()
    }

    @Test
    fun `motta søknad innvilget hendelse`() {
        testRapid.sendTestMessage(søknadInnvilgetJson())
        verify(exactly = 1) {
            personRepositoryMock.håndter(any())
        }
    }
}
