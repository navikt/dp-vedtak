package no.nav.dagpenger.vedtak.modell

import io.kotest.matchers.shouldBe
import no.nav.dagpenger.aktivitetslogg.Aktivitetslogg
import no.nav.dagpenger.vedtak.modell.hendelser.DagpengerAvslåttHendelse
import no.nav.dagpenger.vedtak.modell.vedtak.VedtakObserver
import no.nav.dagpenger.vedtak.modell.visitor.PersonVisitor
import org.junit.jupiter.api.Test
import java.time.LocalDate
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
        val søknadBehandletHendelse = DagpengerAvslåttHendelse(
            behandlingId = UUID.randomUUID(),
            ident = ident,
            virkningsdato = LocalDate.now(),
            dagpengerettighet = Dagpengerettighet.Ordinær,
        )
        person.håndter(
            søknadBehandletHendelse,
        )
        person.håndter(
            søknadBehandletHendelse,
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
