package no.nav.dagpenger.vedtak.modell

import no.nav.dagpenger.vedtak.kontomodell.helpers.desember
import no.nav.dagpenger.vedtak.modell.hendelser.NyRettighetHendelse
import no.nav.dagpenger.vedtak.modell.hendelser.RapporteringHendelse
import no.nav.dagpenger.vedtak.modell.hendelser.RapportertDag
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class PersonTest {

    @Test
    fun `Får NyRettighet og det rapporteres dager på meldekortet`() {
        val person = Person(PersonIdentifikator("12345678910"))

        person.håndter(nyRettighetHendelse())

        assertTrue(person.harVedtak())

        person.håndter(RapporteringHendelse(meldekortDager()))

        assertEquals(1000.0, person.dagerTilBetaling().sumOf { it.beløp.toDouble() })
    }

    private fun meldekortDager() = listOf<RapportertDag>(
        RapportertDag(1 desember 2022),
        RapportertDag(2 desember 2022),
    )

    private fun nyRettighetHendelse() = NyRettighetHendelse()
}
