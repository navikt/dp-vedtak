package no.nav.dagpenger.opplysning.verdier

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class StønadsperiodeTest {
    @Test
    fun `lage stønadsperiode`() {
        val stønadsdager = Stønadsperiode.fraUker(104)
        stønadsdager.dager shouldBe 520
        stønadsdager shouldBe Stønadsperiode.fraUker(104)
        stønadsdager.uker shouldBe 104
    }

    @Test
    fun `kan trekke fra stønadsdager`() {
        val stønadsdager = Stønadsperiode.fraUker(104)
        val forbruk = Stønadsperiode(5)
        stønadsdager - forbruk shouldBe Stønadsperiode(515)
        assertThrows<IllegalArgumentException> { stønadsdager - Stønadsperiode(521) }
    }

    @Test
    fun `kan legge til stønadsdager`() {
        val stønadsdager = Stønadsperiode.fraUker(104)
        val korrigering = Stønadsperiode(5)
        stønadsdager + korrigering shouldBe Stønadsperiode(525)
    }
}
