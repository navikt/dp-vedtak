package no.nav.dagpenger.vedtak.modell.mengder

import no.nav.dagpenger.vedtak.modell.mengder.Enhet.Companion.arbeidsdager
import no.nav.dagpenger.vedtak.modell.mengder.Enhet.Companion.arbeidsuker
import no.nav.dagpenger.vedtak.modell.mengder.Enhet.Companion.dagsprosent
import no.nav.dagpenger.vedtak.modell.mengder.Enhet.Companion.ukeprosent
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

internal class EnhetTest {
    @Test
    internal fun `equality of like units`() {
        assertEquals(8.arbeidsdager, 8.arbeidsdager)
        assertNotEquals(8.arbeidsdager, 6.arbeidsdager)
        assertNotEquals(8.arbeidsdager, Any())
        assertNotEquals(8.arbeidsdager, null)
    }

    @Test
    internal fun `equality of different units`() {
        assertEquals(2.arbeidsuker, 10.arbeidsdager)
        assertNotEquals(8.arbeidsuker, 8.arbeidsdager)
        assertEquals(2.arbeidsuker, (2 * 5).arbeidsdager)
    }

    @Test
    fun `Quantity in hash set`() {
        assert(hashSetOf(8.arbeidsdager).contains(8.arbeidsdager))
        assert(hashSetOf(1.arbeidsuker).contains(5.arbeidsdager))
        assertEquals(1, hashSetOf(8.arbeidsuker, 8.arbeidsuker).size)
    }

    @Test
    fun hash() {
        assertEquals(8.arbeidsdager.hashCode(), 8.arbeidsdager.hashCode())
        assertEquals(1.arbeidsuker.hashCode(), 5.arbeidsdager.hashCode())
    }

    @Test
    fun arithmetic() {
        assertEquals(3.arbeidsuker, 1.arbeidsuker + 10.arbeidsdager)
        assertEquals((-6).arbeidsuker, (-30).arbeidsdager)
        assertEquals((-0.5).arbeidsuker, 5.arbeidsdager - 7.5.arbeidsdager)
    }

    @Test
    fun `cross metric types`() {
        assertNotEquals(1.arbeidsdager, 1.ukeprosent)
    }

    @Test
    fun `incompatible units`() {
        assertThrows<IllegalArgumentException> { 3.arbeidsdager - 4.dagsprosent }
    }
}
