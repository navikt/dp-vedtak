package no.nav.dagpenger.vedtak.modell

import no.nav.dagpenger.vedtak.kontomodell.helpers.desember
import no.nav.dagpenger.vedtak.kontomodell.mengder.Enhet.Companion.arbeidsdager
import no.nav.dagpenger.vedtak.kontomodell.mengder.Enhet.Companion.arbeidsuker
import no.nav.dagpenger.vedtak.modell.Beløp.Companion.beløp
import no.nav.dagpenger.vedtak.modell.hendelser.Ordinær
import no.nav.dagpenger.vedtak.modell.hendelser.RapporteringHendelse
import no.nav.dagpenger.vedtak.modell.hendelser.RapportertDag
import no.nav.dagpenger.vedtak.modell.visitor.PersonVisitor
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

internal class PersonTest {

    private val person = Person(PersonIdentifikator("12345678910"))
    private val testInspektør get() = TestInspektør(person)

    @Test
    fun `Får NyRettighet og det rapporteres dager på meldekortet`() {

        assertEquals("12345678910", testInspektør.id.identifikator())
        val virkningsdato = 18.desember(2022)
        val beslutningstidspunkt = 18.desember(2022).atStartOfDay()

        person.håndter(ordinærRettighetHendelse(virkningsdato, beslutningstidspunkt))

        assertTrue(testInspektør.harVedtak())
        assertEquals(300.beløp, testInspektør.dagsats())
        assertEquals(virkningsdato, testInspektør.virkningsdato())
        assertEquals(beslutningstidspunkt, testInspektør.beslutningstidspunkt())

        person.håndter(RapporteringHendelse(meldekortDager()))

        assertEquals(1000.0, testInspektør.utbetalt())
    }

    private fun meldekortDager() = listOf<RapportertDag>(
        RapportertDag(1 desember 2022),
        RapportertDag(2 desember 2022),
    )

    private fun ordinærRettighetHendelse(virkningsdato: LocalDate, beslutningstidspunkt: LocalDateTime) = Ordinær(
        behandlingsId = UUID.randomUUID(),
        virkningsdato = virkningsdato,
        beslutningstidspunkt = beslutningstidspunkt,
        dagsats = 300.beløp,
        dagpengerPeriode = 52.arbeidsuker,
        ventedager = 5.arbeidsdager
    )

    private class TestInspektør(person: Person) : PersonVisitor {
        lateinit var id: PersonIdentifikator
        private var harVedtak: Boolean = false
        private var utbetalt: Double = 0.0
        private var dagsats: Beløp = 0.0.beløp
        lateinit var virkningsdato: LocalDate
        lateinit var beslutningstidspunkt: LocalDateTime
        init {
            person.accept(this)
        }

        override fun visitPerson(personIdentifikator: PersonIdentifikator) {
            id = personIdentifikator
        }

        override fun visitVedtak(virkningsdato: LocalDate, beslutningstidspunkt: LocalDateTime) {
            harVedtak = true
            this.dagsats = dagsats
            this.virkningsdato = virkningsdato
            this.beslutningstidspunkt = beslutningstidspunkt
        }

        override fun visitDag(dato: LocalDate, beløp: Number) {
            utbetalt += beløp.toDouble()
        }

        override fun visitDagsatsHistorikk(dato: LocalDate, dagsats: Beløp) {
            this.dagsats = dagsats
        }

        fun harVedtak(): Boolean = harVedtak
        fun utbetalt(): Number = utbetalt
        fun dagsats(): Beløp = dagsats
        fun virkningsdato(): LocalDate = virkningsdato
        fun beslutningstidspunkt(): LocalDateTime = beslutningstidspunkt
    }
}
