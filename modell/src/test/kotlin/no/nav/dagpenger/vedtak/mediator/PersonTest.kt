package no.nav.dagpenger.vedtak.mediator

import no.nav.dagpenger.vedtak.modell.Person
import no.nav.dagpenger.vedtak.modell.hendelse.AvslagHendelse
import no.nav.dagpenger.vedtak.modell.hendelse.InnvilgetProsessresultatHendelse
import no.nav.dagpenger.vedtak.modell.hendelse.StansHendelse
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class PersonTest {
    val person = Person()

    @Test
    fun `Person har ikke hatt tidligere, og får melding om ny rettighet`() {
        person.håndter(InnvilgetProsessresultatHendelse())

        assertEquals(1, person.avtaler.size)
        assertEquals(1, person.vedtak.size)
    }

    @Test
    fun `Person har ikke hatt tidligere, og får melding om avslag`() {
        person.håndter(AvslagHendelse())

        assertEquals(1, person.vedtak.size)
        assertEquals(0, person.avtaler.size)
    }

    @Test
    fun `Person har rettighet fra før og skal stanse denne`() {
        person.håndter(InnvilgetProsessresultatHendelse())
        person.håndter(StansHendelse())

        assertEquals(2, person.vedtak.size)
        assertEquals(1, person.avtaler.size)
    }

    @Test
    fun `Nytt prosessresultat fra quiz om endring i fakta`(){
        person.håndter(InnvilgetProsessresultatHendelse())
        person.håndter(InnvilgetProsessresultatHendelse())


    }
}
