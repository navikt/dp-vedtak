package no.nav.dagpenger.vedtak.modell

import no.nav.dagpenger.vedtak.kontomodell.helpers.desember
import no.nav.dagpenger.vedtak.kontomodell.mengder.Enhet.Companion.arbeidsdager
import no.nav.dagpenger.vedtak.kontomodell.mengder.Enhet.Companion.arbeidsuker
import no.nav.dagpenger.vedtak.kontomodell.mengder.RatioMengde
import no.nav.dagpenger.vedtak.modell.Beløp.Companion.beløp
import no.nav.dagpenger.vedtak.modell.hendelser.EndringAvRettighetHendelse
import no.nav.dagpenger.vedtak.modell.hendelser.NyRettighet
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
        val virkningsdato = 1.desember(2022)
        val beslutningstidspunkt = 1.desember(2022).atStartOfDay()

        person.håndter(ordinærRettighetHendelse(virkningsdato, beslutningstidspunkt))

        assertTrue(testInspektør.harVedtak())
        assertEquals(300.beløp, testInspektør.dagsats())
        assertEquals(37.5.beløp, testInspektør.fastsattArbeidstidPerUke())
        assertEquals(3.arbeidsdager, testInspektør.gjenståendeVentedager())
        assertEquals(52.arbeidsuker, testInspektør.gjenståendeDagpengeperiode())
        assertEquals(virkningsdato, testInspektør.virkningsdato())
        assertEquals(beslutningstidspunkt, testInspektør.beslutningstidspunkt())

        person.håndter(RapporteringHendelse(meldekortDager()))

        assertEquals(600.0.beløp, testInspektør.utbetalt())

        person.håndter(
            endringAvRettighetHendelse(
                nySats = 400.beløp,
                virkningsdato = 2.desember(2022),
                beslutningstidspunkt = 2.desember(2022).atStartOfDay(),
            ),
        )

        assertEquals(400.beløp, testInspektør.dagsats())
        assertEquals(700.0.beløp, testInspektør.utbetalt())
    }

    private fun meldekortDager() = listOf<RapportertDag>(
        RapportertDag(1 desember 2022),
        RapportertDag(2 desember 2022),
    )

    private fun ordinærRettighetHendelse(virkningsdato: LocalDate, beslutningstidspunkt: LocalDateTime) = NyRettighet(
        behandlingsId = UUID.randomUUID(),
        virkningsdato = virkningsdato,
        beslutningstidspunkt = beslutningstidspunkt,
        dagsats = 300.beløp,
        fastsattArbeidstidPerUke = 37.5.beløp,
        gjenståendeDagpengeperiode = 52.arbeidsuker,
        gjenståendeVentedager = 3.arbeidsdager,
    )

    private fun endringAvRettighetHendelse(
        virkningsdato: LocalDate,
        beslutningstidspunkt: LocalDateTime,
        nySats: Beløp,
    ) = EndringAvRettighetHendelse(
        behandlingsId = UUID.randomUUID(),
        virkningsdato = virkningsdato,
        beslutningstidspunkt = beslutningstidspunkt,
        dagsats = nySats,
        fastsattArbeidstidPerUke = 37.5.beløp,
        gjenståendeDagpengeperiode = 52.arbeidsuker,
        gjenståendeVentedager = 3.arbeidsdager,
    )

    private class TestInspektør(person: Person) : PersonVisitor {
        lateinit var id: PersonIdentifikator
        private var harVedtak: Boolean = false
        private var utbetalt: Beløp = 0.0.beløp
        private var dagsats: Beløp = 0.0.beløp
        private var fastsattArbeidstidPerUke = 0.0.beløp
        private var gjenståendeVentedager = 0.arbeidsdager
        private var gjenståendeDagpengeperiode = 0.arbeidsdager
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
            this.fastsattArbeidstidPerUke = fastsattArbeidstidPerUke
            this.virkningsdato = virkningsdato
            this.beslutningstidspunkt = beslutningstidspunkt
        }

        override fun visitDag(dato: LocalDate, beløp: Beløp) {
            utbetalt += beløp
        }

        override fun visitDagsatsHistorikk(dato: LocalDate, dagsats: Beløp) {
            this.dagsats = dagsats
        }

        override fun visitFastsattArbeidstidHistorikk(dato: LocalDate, fastsattArbeidstidPerUke: Beløp) {
            this.fastsattArbeidstidPerUke = fastsattArbeidstidPerUke
        }

        override fun visitVentedagerHistorikk(dato: LocalDate, gjenståendeVentedager: RatioMengde) {
            this.gjenståendeVentedager = gjenståendeVentedager
        }

        override fun visitDagpengeperiodeHistorikk(dato: LocalDate, gjenståendeDagpengeperiode: RatioMengde) {
            this.gjenståendeDagpengeperiode = gjenståendeDagpengeperiode
        }

        fun harVedtak(): Boolean = harVedtak
        fun utbetalt(): Beløp = utbetalt
        fun dagsats(): Beløp = dagsats
        fun fastsattArbeidstidPerUke(): Beløp = fastsattArbeidstidPerUke
        fun gjenståendeVentedager(): RatioMengde = gjenståendeVentedager
        fun gjenståendeDagpengeperiode(): RatioMengde = gjenståendeDagpengeperiode
        fun virkningsdato(): LocalDate = virkningsdato
        fun beslutningstidspunkt(): LocalDateTime = beslutningstidspunkt
    }
}
