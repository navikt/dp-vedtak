package no.nav.dagpenger.vedtak.modell

import no.nav.dagpenger.vedtak.modell.tid.quantity.RatioQuantity.Companion.zero
import no.nav.dagpenger.vedtak.modell.tid.quantity.celsius
import no.nav.dagpenger.vedtak.modell.tid.quantity.chains
import no.nav.dagpenger.vedtak.modell.tid.quantity.cups
import no.nav.dagpenger.vedtak.modell.tid.quantity.days
import no.nav.dagpenger.vedtak.modell.tid.quantity.fahrenheit
import no.nav.dagpenger.vedtak.modell.tid.quantity.feet
import no.nav.dagpenger.vedtak.modell.tid.quantity.furlongs
import no.nav.dagpenger.vedtak.modell.tid.quantity.gallons
import no.nav.dagpenger.vedtak.modell.tid.quantity.gasMark
import no.nav.dagpenger.vedtak.modell.tid.quantity.inches
import no.nav.dagpenger.vedtak.modell.tid.quantity.kelvin
import no.nav.dagpenger.vedtak.modell.tid.quantity.links
import no.nav.dagpenger.vedtak.modell.tid.quantity.miles
import no.nav.dagpenger.vedtak.modell.tid.quantity.ounces
import no.nav.dagpenger.vedtak.modell.tid.quantity.pints
import no.nav.dagpenger.vedtak.modell.tid.quantity.quarts
import no.nav.dagpenger.vedtak.modell.tid.quantity.rankine
import no.nav.dagpenger.vedtak.modell.tid.quantity.tablespoons
import no.nav.dagpenger.vedtak.modell.tid.quantity.teaspoons
import no.nav.dagpenger.vedtak.modell.tid.quantity.weeks
import no.nav.dagpenger.vedtak.modell.tid.quantity.yards
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class QuanitityTest {

    @Test
    internal fun `tidstester er moro`() {
        assertEquals(7.days, 1.weeks)
    }


    @Test
    internal fun `equality of like units`() {
        assertEquals(4.0.tablespoons, 4.0.tablespoons)
        assertNotEquals(4.tablespoons, 6.tablespoons)
        assertNotEquals(4.0.tablespoons, Any())
        assertNotEquals(4.0.tablespoons, null)
    }

    @Test
    internal fun `equality of different units`() {
        assertEquals(8.tablespoons, 0.5.cups)
        assertEquals(768.teaspoons, 1.gallons)
        assertNotEquals(4.0.tablespoons, 4.0.teaspoons)
        assertEquals(18.inches, 0.5.yards)
        assertEquals(50.links, 11.yards)
        assertEquals(1.miles, (12 * 5280).inches)
    }

    @Test
    internal fun `set operations`() {
        assertTrue(4.0.tablespoons in hashSetOf(4.0.tablespoons))
        assertTrue(0.25.cups in hashSetOf(4.0.tablespoons))
        assertEquals(1, hashSetOf(4.0.tablespoons, 4.0.tablespoons).size)
        assertEquals(1, hashSetOf(4.0.tablespoons, 2.ounces).size)
    }

    @Test
    internal fun hash() {
        assertEquals(4.0.tablespoons.hashCode(), 4.0.tablespoons.hashCode())
        assertEquals(8.tablespoons.hashCode(), 0.5.cups.hashCode())
        assertEquals(18.inches.hashCode(), 0.5.yards.hashCode())
        assertEquals(-(4.feet), 24.inches - 2.yards)
        assertEquals(8.chains, 1.furlongs - 44.yards)
        assertEquals(50.fahrenheit.hashCode(), 10.celsius.hashCode())
    }

    @Test
    internal fun arithmetic() {
        assertEquals(0.5.quarts, +(6.tablespoons) + 13.ounces)
        assertEquals((-6).tablespoons, -(6.tablespoons))
        assertEquals((-0.5).pints, 10.tablespoons - 13.ounces)
    }

    @Test
    internal fun `cross metric type inequality`() {
        assertNotEquals(1.inches, 1.teaspoons)
        assertNotEquals(4.ounces, 2.feet)
    }

    @Test
    internal fun `incompatible units`() {
        assertThrows<IllegalArgumentException> { 3.yards - 4.tablespoons }
    }

    @Test
    internal fun temperatures() {
        assertBidirectionalEquality(0.celsius, 32.fahrenheit)
        assertBidirectionalEquality(10.celsius, 50.fahrenheit)
        assertBidirectionalEquality(100.celsius, 212.fahrenheit)
        assertBidirectionalEquality((-40).celsius, (-40).fahrenheit)
        assertBidirectionalEquality(325.fahrenheit, 3.gasMark)
        assertBidirectionalEquality(0.celsius, 273.15.kelvin)
        assertBidirectionalEquality(50.fahrenheit, 283.15.kelvin)
        assertBidirectionalEquality(50.fahrenheit, 509.67.rankine)
    }

    @Test
    internal fun temperatureArithmetic() {
        // The following should not compile!
//        10.celsius - 32.fahrenheit
    }

    @Test
    internal fun `universal zero`() {
        assertEquals(zero, zero)
        assertEquals(zero, +(zero))
        assertEquals(zero, -(zero))
        assertEquals(zero, zero + zero)
        assertEquals(4.tablespoons, 4.tablespoons + zero)
        assertEquals(4.tablespoons, zero + 4.tablespoons)
        assertEquals(zero, zero - zero)
        assertEquals(4.tablespoons, 4.tablespoons - zero)
        assertEquals((-4).tablespoons, zero - 4.tablespoons)
        assertTrue(4.tablespoons > zero)
        assertTrue(zero < 4.tablespoons)
        assertTrue((-4.tablespoons) < zero)
        assertEquals(0, zero.compareTo(zero))
        assertEquals(0, zero.compareTo(0.tablespoons))
        assertEquals(0, 0.tablespoons.compareTo(zero))
        assertEquals(0.tablespoons, zero)
        assertEquals(zero, 0.tablespoons)
        assertEquals(zero.hashCode(), 0.tablespoons.hashCode())
    }

    private fun <T> assertBidirectionalEquality(left: T, right: T) {
        assertEquals(left, right)
        assertEquals(right, left)
    }
}