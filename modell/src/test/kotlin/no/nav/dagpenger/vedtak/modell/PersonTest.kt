package no.nav.dagpenger.vedtak.modell

import no.nav.dagpenger.vedtak.modell.hendelser.NyRettighetHendelse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class PersonTest {

    @Test
    fun `skal motta ny rettighet`() {
        val person = Person(PersonIdentifikator("12345678910"))

        person.håndter(nyRettighetHendelse())

        assertTrue(person.harVedtak())
    }

    private fun nyRettighetHendelse() = NyRettighetHendelse()
}
