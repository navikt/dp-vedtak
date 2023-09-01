package no.nav.dagpenger.vedtak.modell

import io.kotest.matchers.shouldBe
import no.nav.dagpenger.aktivitetslogg.Aktivitetslogg
import no.nav.dagpenger.vedtak.modell.hendelser.SøknadBehandletOgAvslåttHendelse
import no.nav.dagpenger.vedtak.modell.vedtak.VedtakObserver
import no.nav.dagpenger.vedtak.modell.vedtak.rettighet.Ordinær
import no.nav.dagpenger.vedtak.modell.visitor.PersonVisitor
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import java.util.UUID

internal class PersonTest {

    private val ident = "12345678910"
    private val testObservatør = TestObservatør()
    private val person = Person(PersonIdentifikator(ident)).also {
        it.addObserver(testObservatør)
    }
    private val inspektør get() = PersonInspektør(person)

    @Test
    fun `behandling med samme id skal bare behandles 1 gang og logge en warning aktivitetsloggen`() {
        val søknadBehandletOgAvslåttHendelse = SøknadBehandletOgAvslåttHendelse(
            meldingsreferanseId = UUID.randomUUID(),
            behandlingId = UUID.randomUUID(),
            sakId = "SAK_NUMMER_1",
            ident = ident,
            vedtakstidspunkt = LocalDateTime.now().truncatedTo(ChronoUnit.MILLIS),
            virkningsdato = LocalDate.now(),
            hovedrettighet = Ordinær(false),
        )
        person.håndter(
            søknadBehandletOgAvslåttHendelse,
        )
        person.håndter(
            søknadBehandletOgAvslåttHendelse,
        )

        testObservatør.vedtak.size shouldBe 1
        inspektør.aktivitetslogg.aktivitetsteller() shouldBe 2
    }

    private class TestObservatør : PersonObserver {

        val vedtak = mutableListOf<VedtakObserver.VedtakFattet>()
        override fun vedtakFattet(ident: String, vedtakFattet: VedtakObserver.VedtakFattet) {
            vedtak.add(vedtakFattet)
        }
    }

    private class PersonInspektør(private val person: Person) : PersonVisitor {

        lateinit var aktivitetslogg: Aktivitetslogg
        init {
            person.accept(this)
        }

        override fun postVisitAktivitetslogg(aktivitetslogg: Aktivitetslogg) {
            this.aktivitetslogg = aktivitetslogg
        }
    }
}
