package no.nav.dagpenger

import no.nav.dagpenger.hendelse.SøknadOmNyRettighetHendelse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

internal class VedtakTest {

    @Test
    fun `Skal kunne opprettet sak`() {
        val søknadshendelse = SøknadOmNyRettighetHendelse(søknadsReferanse = "referanse", fnr ="1234567890")
        val person = Person()
        person.søk(søknadshendelse)
        assertTrue(person.harSak("referanse"))
    }

    @Test
    fun `En sak skal ikke ha duplikate søknader `() {
        val søknadshendelse = SøknadOmNyRettighetHendelse(søknadsReferanse = "referanse", fnr ="1234567890")
        val person = Person()
        person.søk(søknadshendelse)
        person.søk(søknadshendelse)
        assertTrue(person.harSak("referanse"))
    }
}