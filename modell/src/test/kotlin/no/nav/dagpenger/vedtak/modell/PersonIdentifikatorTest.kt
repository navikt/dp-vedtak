package no.nav.dagpenger.vedtak.modell

import no.nav.dagpenger.vedtak.modell.PersonIdentifikator.Companion.tilPersonIdentfikator
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class PersonIdentifikatorTest {
    @Test
    fun `personidentifikator består av 11 siffer`() {
        Assertions.assertDoesNotThrow { "12345678901".tilPersonIdentfikator() }
        assertThrows<IllegalArgumentException> { "123".tilPersonIdentfikator() }
        assertThrows<IllegalArgumentException> { "ident".tilPersonIdentfikator() }
    }

    @Test
    fun ` likhet `() {
        val personIdent = "12345678901".tilPersonIdentfikator()
        assertEquals(personIdent, personIdent)
        assertEquals(personIdent.hashCode(), personIdent.hashCode())
        assertEquals(personIdent, "12345678901".tilPersonIdentfikator())
        assertEquals(personIdent.hashCode(), "12345678901".tilPersonIdentfikator().hashCode())
        assertNotEquals(personIdent, "22345678901".tilPersonIdentfikator())
        assertNotEquals("22345678901".tilPersonIdentfikator(), personIdent)
        assertNotEquals(personIdent, Any())
        assertNotEquals(personIdent, null)
    }
}
