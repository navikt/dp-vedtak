package no.nav.dagpenger.opplysning.verdier

import org.javamoney.moneta.Money
import org.junit.jupiter.api.Test
import java.math.BigDecimal

class BeløpTest {
    @Test
    fun blupr() {
        val b = Money.of(BigDecimal("10.00000000000000000006"), "NOK")
        println(b.toString())
    }
}
