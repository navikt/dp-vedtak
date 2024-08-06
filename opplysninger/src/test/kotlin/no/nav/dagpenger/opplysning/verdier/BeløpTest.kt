package no.nav.dagpenger.opplysning.verdier

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.javamoney.moneta.Money
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class BeløpTest {
    @Test
    fun blupr() {
        val b = Money.of(BigDecimal("10.00000000000000000006"), "NOK")
        println(b.toString())
    }

    @Test
    fun `likhet`() {
        val beløp = Beløp(100.0)
        beløp shouldBe Beløp(100.0)
        beløp shouldBe beløp
        beløp shouldNotBe Beløp(200.0)
        beløp shouldNotBe Beløp(100.1)
        beløp shouldNotBe Any()
    }

    @Test
    fun `kan addere og multiplisere`() {
        val beløp = Beløp(100.0)
        val beløp2 = Beløp(200.0)
        val beløp3 = Beløp(300.0)
        beløp + beløp2 shouldBe beløp3
        beløp * 3.0 shouldBe beløp3
    }

    @Test
    fun `kan dele`() {
        val beløp = Beløp(100.0)
        val beløp2 = Beløp(200.0)
        beløp2 / 2.0 shouldBe beløp

        Beløp(100.0) / 3.0 shouldBe Beløp("NOK 33.33333333333333333333")
    }
}
