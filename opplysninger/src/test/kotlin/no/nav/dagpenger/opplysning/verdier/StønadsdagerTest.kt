package no.nav.dagpenger.opplysning.verdier

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class StønadsdagerTest {
    @Test
    fun `lage stønadsperiode`() {
        val stønadsdager = Stønadsperiode(104).tilStønadsdager()
        stønadsdager.dager shouldBe 520
        stønadsdager shouldBe Stønadsperiode(104).tilStønadsdager()
    }

    @Test
    fun `kan trekke fra stønadsdager`() {
        val stønadsdager = Stønadsperiode(104).tilStønadsdager()
        val forbruk = Stønadsdager(5)
        stønadsdager - forbruk shouldBe Stønadsdager(515)
        assertThrows<IllegalArgumentException> { stønadsdager - Stønadsdager(521) }
    }

    @Test
    fun `kan legge til stønadsdager`() {
        val stønadsdager = Stønadsperiode(104).tilStønadsdager()
        val korrigering = Stønadsdager(5)
        stønadsdager + korrigering shouldBe Stønadsdager(525)
    }
}
