package no.nav.dagpenger.vedtak.modell

import no.nav.dagpenger.vedtak.kontomodell.helpers.desember
import no.nav.dagpenger.vedtak.modell.hendelser.NyRettighetHendelse
import no.nav.dagpenger.vedtak.modell.hendelser.RapporteringHendelse
import no.nav.dagpenger.vedtak.modell.hendelser.RapportertDag
import no.nav.dagpenger.vedtak.modell.visitor.PersonVisitor
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime

internal class PersonTest {

    @Test
    fun `Får NyRettighet og det rapporteres dager på meldekortet`() {
        val person = Person(PersonIdentifikator("12345678910"))

        val testInspektør = TestInspektør(person)

        assertEquals("12345678910", testInspektør.id.identifikator())

        person.håndter(nyRettighetHendelse())

        TestInspektør(person).also { assertTrue(it.harVedtak()) }

        person.håndter(RapporteringHendelse(meldekortDager()))

        assertEquals(1000.0, person.dagerTilBetaling().sumOf { it.beløp.toDouble() })
    }

    private fun meldekortDager() = listOf<RapportertDag>(
        RapportertDag(1 desember 2022),
        RapportertDag(2 desember 2022),
    )

    private fun nyRettighetHendelse() = NyRettighetHendelse()

    private class TestInspektør(person: Person) : PersonVisitor {
        lateinit var id: PersonIdentifikator
        var harVedtak: Boolean = false
        init {
            person.accept(this)
        }

        override fun visitPerson(personIdentifikator: PersonIdentifikator) {
            id = personIdentifikator
        }

        override fun visitVedtak(virkningsdato: LocalDate, beslutningstidspunkt: LocalDateTime) {
            harVedtak = true
        }

        fun harVedtak(): Boolean = harVedtak
    }
}
