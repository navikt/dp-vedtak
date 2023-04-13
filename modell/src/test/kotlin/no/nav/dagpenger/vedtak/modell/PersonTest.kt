package no.nav.dagpenger.vedtak.modell

import no.nav.dagpenger.vedtak.modell.hendelser.DagpengerAvslåttHendelse
import no.nav.dagpenger.vedtak.modell.vedtak.VedtakObserver
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.util.UUID

internal class PersonTest {

    private val ident = "12345678910"
    private val testObservatør = TestObservatør()
    private val person = Person(PersonIdentifikator(ident)).also {
        it.addObserver(testObservatør)
    }

    @Test
    fun `behandling med samme id skal bare behandles 1 gang`() {
        val søknadBehandletHendelse = DagpengerAvslåttHendelse(
            behandlingId = UUID.randomUUID(),
            ident = ident,
            virkningsdato = LocalDate.now(),

        )
        person.håndter(
            søknadBehandletHendelse,
        )
        person.håndter(
            søknadBehandletHendelse,
        )

        assertEquals(1, testObservatør.vedtak.size)
    }

    private class TestObservatør : PersonObserver {

        val vedtak = mutableListOf<VedtakObserver.VedtakFattet>()
        override fun vedtaktFattet(ident: String, vedtakFattet: VedtakObserver.VedtakFattet) {
            vedtak.add(vedtakFattet)
        }
    }
}
