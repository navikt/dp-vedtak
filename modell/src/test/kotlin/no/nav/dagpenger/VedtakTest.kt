package no.nav.dagpenger

import no.nav.dagpenger.hendelse.ProsessResultatHendelse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class VedtakTest {
    @Test
    fun `Har fått prosessresutalt fra quiz der alt er er oppfylt  innvilgelse`() {
        val person = Person()
        // Ute i verden ett sted (mediator)
        val hendelse = ProsessResultatHendelse(utfall = true)

        person.håndter(hendelse)

        assertTrue(person.harDagpenger())
    }

    @Test
    fun `Har fått prosessresutalt fra quiz der noe ikke er oppfylt  avslag`() {
        val person = Person()
        // Ute i verden ett sted (mediator)
        val hendelse = ProsessResultatHendelse(utfall = false)

        person.håndter(hendelse)
    }

    @Test
    fun `Har ikke sendt meldekort`() {
        val person = Person()

        // Ute i verden ett sted (mediator)
        // val hendelse = ManglendeMeldekortHendelse()

        // person.håndter(hendelse)
    }
}
