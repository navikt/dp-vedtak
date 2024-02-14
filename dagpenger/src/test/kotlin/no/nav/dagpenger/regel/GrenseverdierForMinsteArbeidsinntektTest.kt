package no.nav.dagpenger.regel

import no.nav.dagpenger.dato.mars
import no.nav.dagpenger.dato.oktober
import no.nav.dagpenger.regel.GrenseverdierForMinsteArbeidsinntekt.finnTerskel
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class GrenseverdierForMinsteArbeidsinntektTest {
    @Test
    fun `at grensedatoer fungerer som forventet`() {
        with(finnTerskel(19.mars(2020))) {
            assertEquals(1.5, nedre)
            assertEquals(3.0, øvre)
        }
        with(finnTerskel(20.mars(2020))) {
            assertEquals(0.75, nedre)
            assertEquals(2.25, øvre)
        }
        with(finnTerskel(30.oktober(2020))) {
            assertEquals(0.75, nedre)
            assertEquals(2.25, øvre)
        }
        with(finnTerskel(31.oktober(2020))) {
            assertEquals(1.5, nedre)
            assertEquals(3.0, øvre)
        }
    }
}
