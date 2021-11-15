package no.nav.dagpenger.vedtak.mediator

import no.nav.dagpenger.vedtak.modell.Person.PersonIdent
import no.nav.dagpenger.vedtak.modell.hendelse.NyRettighetHendelse
import no.nav.dagpenger.vedtak.modell.Person
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import java.util.UUID

internal class PersonTest {
    val person = Person(PersonIdent("12345678909"))

    @Test
    fun `Person med ident har ingen rettighet som kan gjennopptas`() {
        // person.håndter(HarRettighetBehovHendelse("12345678909"))
    }

    @Test
    fun `Person med indent har en rettighet og svarer med søknadsid`() {
    }

    @Test
    fun `Oppretter ny avtale`() {
        person.håndter(NyRettighetHendelse(UUID.randomUUID().toString()))
        assertNotNull(person.aktivAvtale())
    }

    @Test
    fun `Har fått prosessresultat fra quiz der noe ikke er oppfylt avslag`() {
    }

    @Test
    fun `Har ikke sendt meldekort, stanser vedtak`() {
    }

    @Test
    fun `Oppretter gjennopptaksvedtak`() {
    }

    @Test
    fun `Kan omgjøre vedtak`() {
    }
}
