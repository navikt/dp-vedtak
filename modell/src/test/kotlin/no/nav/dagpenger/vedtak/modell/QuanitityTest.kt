package no.nav.dagpenger.vedtak.modell

import no.nav.dagpenger.vedtak.modell.tid.quantity.RatioQuantity.Companion.zero
import no.nav.dagpenger.vedtak.modell.tid.quantity.dager
import no.nav.dagpenger.vedtak.modell.tid.quantity.uker
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class QuanitityTest {

    @Test
    fun `tidstester er moro`() {
        assertEquals(5.dager, 1.uker)
    }

    @Test
    fun `universal zero`() {
        assertEquals(zero, zero)
        assertEquals(zero, +(zero))
        assertEquals(zero, -(zero))
        assertEquals(zero, zero + zero)
        assertEquals(zero, zero - zero)
        assertEquals(0, zero.compareTo(zero))
    }

    private fun <T> assertBidirectionalEquality(left: T, right: T) {
        assertEquals(left, right)
        assertEquals(right, left)
    }
}