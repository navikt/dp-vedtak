package no.nav.dagpenger

import no.nav.dagpenger.hendelse.GjenopptakHendelse
import no.nav.dagpenger.hendelse.ManglendeMeldekortHendelse
import no.nav.dagpenger.hendelse.ProsessResultatHendelse
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class VedtakTest {
    @Test
    fun `Har fått prosessresultat fra quiz der alt er er oppfylt  innvilgelse`() {
        val person = Person()
        val hendelse = ProsessResultatHendelse(utfall = true)

        person.håndter(hendelse)

        assertTrue(person.harDagpenger())
    }

    @Test
    fun `Har fått prosessresultat fra quiz der noe ikke er oppfylt avslag`() {
        val person = Person()
        val hendelse = ProsessResultatHendelse(utfall = false)

        person.håndter(hendelse)
        assertFalse(person.harDagpenger())
    }

    @Test
    fun `Har ikke sendt meldekort`() {
        val person = Person().apply {
            håndter(ProsessResultatHendelse(utfall = true))
        }

        // Ute i verden ett sted (mediator)
        val hendelse = ManglendeMeldekortHendelse()

        person.håndter(hendelse)
        assertFalse(person.harDagpenger())
    }

    @Test
    fun `Skal kunne gjenoppta`() {
        val person = Person().apply {
            håndter(ProsessResultatHendelse(utfall = true))
            håndter(ManglendeMeldekortHendelse())
        }

        // Ute i verden ett sted (mediator)
        val hendelse = GjenopptakHendelse(utfall = true)

        person.håndter(hendelse)
        assertTrue(person.harDagpenger())
    }
}
