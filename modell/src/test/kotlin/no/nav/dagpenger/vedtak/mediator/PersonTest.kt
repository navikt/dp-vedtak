package no.nav.dagpenger.vedtak.mediator

import no.nav.dagpenger.vedtak.modell.Person
import no.nav.dagpenger.vedtak.modell.hendelse.AvslagHendelse
import no.nav.dagpenger.vedtak.modell.hendelse.InnvilgetProsessresultatHendelse
import no.nav.dagpenger.vedtak.modell.hendelse.NyttBarnVurdertHendelse
import no.nav.dagpenger.vedtak.modell.hendelse.StansHendelse
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class PersonTest {
    val person = Person()

    @Test
    fun `Person har ikke hatt tidligere, og får melding om ny rettighet`() {
        innvilgVedtak()
        assertEquals(1, person.avtaler.size)
        assertEquals(1, person.vedtak.size)
        assertEquals(500.0,person.avtaler.last().sats())
    }

    @Test
    fun `Person har ikke hatt tidligere, og får melding om avslag`() {
        person.håndter(AvslagHendelse())

        assertEquals(1, person.vedtak.size)
        assertEquals(0, person.avtaler.size)
    }

    @Test
    fun `Person har rettighet fra før og skal stanse denne`() {
        innvilgVedtak()
        person.håndter(StansHendelse())

        assertEquals(2, person.vedtak.size)
        assertEquals(1, person.avtaler.size)
    }


    @Test
    fun `Nytt barn fører til økt sats`() {
        innvilgVedtak()
        person.håndter(NyttBarnVurdertHendelse(resultat = true, sats = 1000.0))
        assertEquals(1000.0, person.avtaler.last().sats())
    }

    @Test
    fun `Nytt barn fører ikke til økt sats`() {


    }

    fun innvilgVedtak(sats: Double = 500.0) {
        person.håndter(InnvilgetProsessresultatHendelse(sats = sats))
    }
}
