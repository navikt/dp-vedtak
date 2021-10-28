package no.nav.dagpenger

import no.nav.dagpenger.hendelse.GjenopptakHendelse
import no.nav.dagpenger.hendelse.ManglendeMeldekortHendelse
import no.nav.dagpenger.hendelse.ProsessResultatHendelse
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class VedtakTest {
    val ident= Person.PersonIdent("12345678910")

    //TODO: Rapids and rivers test som sjekker at søknadsid blir sendt ut på kafka
    @Test
    fun `Person med ident har ingen rettighet som kan gjennopptas`(){
        val person = Person(ident)
        assertNull(person.rettighetTilhørendePerson())
        person.also {
            it.håndter(ProsessResultatHendelse(true))
        }
        assertNull(person.rettighetTilhørendePerson())

    }

    @Test
    fun `Person med indent har en rettighet og svarer med søknadsid`(){
        Person(ident).also {
            it.håndter(ProsessResultatHendelse(true))
            it.håndter(ManglendeMeldekortHendelse())

            assertNotNull(it.rettighetTilhørendePerson())
        }
    }

    @Test
    fun `Har fått prosessresultat fra quiz der alt er er oppfylt  innvilgelse`() {
        val person = Person(ident)
        val hendelse = ProsessResultatHendelse(utfall = true)

        person.håndter(hendelse)

        assertTrue(person.harDagpenger())
    }

    @Test
    fun `Har fått prosessresultat fra quiz der noe ikke er oppfylt avslag`() {
        val person = Person(ident)
        val hendelse = ProsessResultatHendelse(utfall = false)

        person.håndter(hendelse)
        assertFalse(person.harDagpenger())
    }

    @Test
    fun `Har ikke sendt meldekort, stanser vedtak`() {
        val person = Person(ident).apply {
            håndter(ProsessResultatHendelse(utfall = true))
        }

        // Ute i verden ett sted (mediator)
        val hendelse = ManglendeMeldekortHendelse()

        person.håndter(hendelse)
        assertFalse(person.harDagpenger())
    }

    @Test
    fun `Oppretter gjennopptaksvedtak`() {
        val person = Person(ident).apply {
            håndter(ProsessResultatHendelse(utfall = true))
            håndter(ManglendeMeldekortHendelse())
        }

        // Ute i verden ett sted (mediator)
        val hendelse = GjenopptakHendelse(utfall = true)

        person.håndter(hendelse)
        assertTrue(person.harDagpenger())
    }
}
