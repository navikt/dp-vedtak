package no.nav.dagpenger.vedtak

import io.kotest.matchers.collections.shouldBeEmpty
import io.mockk.every
import io.mockk.mockk
import no.nav.dagpenger.vedtak.db.InMemoryMeldingRepository
import no.nav.dagpenger.vedtak.mediator.HendelseMediator
import no.nav.dagpenger.vedtak.mediator.Meldingsfabrikk.rettighetBehandletOgInnvilgetJson
import no.nav.dagpenger.vedtak.mediator.PersonMediator
import no.nav.dagpenger.vedtak.mediator.persistens.PersonRepository
import no.nav.dagpenger.vedtak.mediator.vedtak.VedtakFattetObserver
import no.nav.dagpenger.vedtak.modell.PersonIdentifikator.Companion.tilPersonIdentfikator
import no.nav.dagpenger.vedtak.modell.PersonObserver
import no.nav.dagpenger.vedtak.modell.vedtak.VedtakObserver
import no.nav.helse.rapids_rivers.testsupport.TestRapid
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class PersonMediatorKonsistensTest {
    private val testRapid = TestRapid()
    private val testObservatør = TestObservatør()
    private val personRepository = mockk<PersonRepository>()

    init {
        HendelseMediator(
            rapidsConnection = testRapid,
            personMediator =
                PersonMediator(
                    aktivitetsloggMediator = mockk(relaxed = true),
                    personRepository = personRepository,
                    personObservers = listOf(VedtakFattetObserver(testRapid), testObservatør),
                ),
            hendelseRepository = InMemoryMeldingRepository(),
        )
    }

    @Test
    fun `venter til aggregatet er lagret før observere blir kalt`() {
        val feilendeIdent = "23456789101"
        every { personRepository.hent(feilendeIdent.tilPersonIdentfikator()) } returns null
        every { personRepository.lagre(any()) } throws RuntimeException("blaaaa")
        assertThrows<RuntimeException> { testRapid.sendTestMessage(rettighetBehandletOgInnvilgetJson(ident = feilendeIdent)) }
        testObservatør.vedtak.shouldBeEmpty()
    }

    private class TestObservatør : PersonObserver {
        val vedtak = mutableListOf<VedtakObserver.VedtakFattet>()

        override fun vedtakFattet(
            ident: String,
            vedtakFattet: VedtakObserver.VedtakFattet,
        ) {
            vedtak.add(vedtakFattet)
        }
    }
}
