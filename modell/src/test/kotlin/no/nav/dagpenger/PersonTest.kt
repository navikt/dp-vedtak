package no.nav.dagpenger

import no.nav.dagpenger.Person.PersonIdent
import no.nav.dagpenger.hendelse.HarRettighetBehovHendelse
import no.nav.dagpenger.hendelse.NyRettighetHendelse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.*

internal class PersonTest {
    val person = Person(PersonIdent("12345678909"))

    @Test
    fun `Person med ident har ingen rettighet som kan gjennopptas`(){
        person.håndter(HarRettighetBehovHendelse("12345678909"))
    }

    @Test
    fun `Person med indent har en rettighet og svarer med søknadsid`(){}

    @Test
    fun `Oppretter ny avtale`() {
        person.håndter(NyRettighetHendelse(UUID.randomUUID().toString()))
        assertNotNull(person.aktivAvtale())
    }

    @Test
    fun `Har fått prosessresultat fra quiz der noe ikke er oppfylt avslag`() {}

    @Test
    fun `Har ikke sendt meldekort, stanser vedtak`() {}

    @Test
    fun `Oppretter gjennopptaksvedtak`() {}

    @Test
    fun `Kan omgjøre vedtak`() {}

}
