package no.nav.dagpenger.vedtak.mediator.mottak

import io.kotest.assertions.throwables.shouldThrow
import io.mockk.Runs
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.verify
import no.nav.dagpenger.vedtak.mediator.IHendelseMediator
import no.nav.dagpenger.vedtak.mediator.Meldingsfabrikk.rettighetBehandletOgAvslåttJson
import no.nav.dagpenger.vedtak.mediator.Meldingsfabrikk.rettighetBehandletOgInnvilgetJson
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

internal class RettighetBehandletMottakTest {
    private val hendelseMediatorMock =
        mockk<IHendelseMediator>().also {
            every { it.behandle(any(), any<RettighetBehandletHendelseMessage>(), any()) } just Runs
        }
    private val testRapid =
        TestRapid().also {
            RettighetBehandletMottak(it, hendelseMediatorMock)
        }

    @BeforeEach
    fun setUp() {
        testRapid.reset()
    }

    @Test
    fun `motta søknad om dagpenger innvilget hendelse`() {
        testRapid.sendTestMessage(rettighetBehandletOgInnvilgetJson())
        verify(exactly = 1) {
            hendelseMediatorMock.behandle(any(), any<RettighetBehandletHendelseMessage>(), any())
        }
    }

    @Test
    fun `motta søknad om dagpenger avslått hendelse`() {
        testRapid.sendTestMessage(rettighetBehandletOgAvslåttJson())
        verify(exactly = 1) {
            hendelseMediatorMock.behandle(any(), any<RettighetBehandletHendelseMessage>(), any())
        }
    }

    @Test
    fun `Kan kun håndtere rettighetstype Ordinær`() {
        shouldThrow<IllegalArgumentException> {
            testRapid.sendTestMessage(
                rettighetBehandletOgInnvilgetJson(
                    rettighetstype = "Permittering",
                ),
            )
        }
    }

    @Test
    fun `avslå meldinger som ikke validerer`() {
        testRapid.sendTestMessage(rettighetBehandletOgInnvilgetJson(rettighetstype = "bla bla"))
        verify(exactly = 0) {
            hendelseMediatorMock.behandle(any(), any<RettighetBehandletHendelseMessage>(), any())
        }
    }
}
