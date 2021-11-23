package no.nav.dagpenger.vedtak.modell

import no.nav.dagpenger.vedtak.modell.tid.quantity.arbeidsdager
import no.nav.dagpenger.vedtak.modell.tid.quantity.arbeidsuker
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class TidsenhetTest {

    @Test
    fun `tidstester er moro`() {
        assertEquals(5.arbeidsdager, 1.arbeidsuker)
        assertEquals(1.arbeidsuker, 1.arbeidsdager + 4.arbeidsdager)
        // assertEquals(2.arbeidsuker, 4.arbeidsuker - 10.arbeidsdager)
    }

    /*
    @Test
    fun `universal zero`() {
        assertEquals(zero, zero)
        assertEquals(zero, +(zero))
        assertEquals(zero, -(zero))
        assertEquals(zero, zero + zero)
        assertEquals(zero, zero - zero)
        assertEquals(0, zero.compareTo(zero))
    }*/

    private fun <T> assertBidirectionalEquality(left: T, right: T) {
        assertEquals(left, right)
        assertEquals(right, left)
    }
}
