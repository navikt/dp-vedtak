package no.nav.dagpenger.vedtak.modell.entitet

import no.nav.dagpenger.vedtak.modell.entitet.Timer.Companion.timer
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class ProsentTest {

    @Test
    fun `gyldige prosenter`() {
        val prosent = Prosent(50)
        assertEquals(prosent, prosent)
        assertEquals(prosent, Prosent(50))
        assertEquals(prosent.hashCode(), prosent.hashCode())
        assertNotEquals(prosent, Prosent(30))
        assertNotEquals(prosent.hashCode(), Prosent(30).hashCode())
        assertNotEquals(prosent, null)
        assertNotEquals(prosent, Any())
        assertThrows<IllegalArgumentException> { Prosent(-10) }
    }

    @Test
    fun `prosent av timer`() {
        val prosent = Prosent(50)
        assertEquals(20.timer, prosent av 40.timer)
    }
}
