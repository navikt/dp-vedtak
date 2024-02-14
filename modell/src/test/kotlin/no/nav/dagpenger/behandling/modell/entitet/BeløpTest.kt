package no.nav.dagpenger.behandling.modell.entitet

import no.nav.dagpenger.behandling.modell.entitet.Beløp.Companion.beløp
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test

class BeløpTest {
    @Test
    fun `likhet test`() {
        assertEquals(1.33.beløp, 1.33.beløp)
        assertNotEquals(1.33.beløp, 1.32.beløp)
        assertEquals(2.beløp, 1.beløp + 1.beløp)
        assertEquals(1.beløp, 2.beløp - 1.beløp)
        assertEquals(6.beløp, 3.beløp * 2.beløp)
        assertEquals(6.25.beløp, 25.beløp / 4.beløp)
        assertNotEquals(1.beløp, Any())
        assertNotEquals(1.beløp, null)
    }
}
