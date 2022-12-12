package no.nav.dagpenger.vedtak.modell

import no.nav.dagpenger.vedtak.modell.hendelser.MeldekortDag
import no.nav.dagpenger.vedtak.modell.hendelser.NyRettighetHendelse
import no.nav.dagpenger.vedtak.modell.hendelser.RapporteringHendelse
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDate

internal class PersonTest {

    @Test
    fun `Får NyRettighet og det rapporteres dager på meldekortet`() {
        val person = Person(PersonIdentifikator("12345678910"))

        person.håndter(nyRettighetHendelse())

        assertTrue(person.harVedtak())

        person.håndter(RapporteringHendelse(meldekortDager()))

        assertEquals(1000.0, person.dagerTilBetaling().sumOf { it.beløp.toDouble() })
    }

    private fun meldekortDager() = listOf<MeldekortDag>(
        MeldekortDag(LocalDate.now().minusDays(1)),
        MeldekortDag(LocalDate.now()),
    )

    private fun nyRettighetHendelse() = NyRettighetHendelse()
}
