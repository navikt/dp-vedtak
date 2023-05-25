package no.nav.dagpenger.vedtak.modell.entitet

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Test

class StønadsperiodeTest {

    @Test
    fun likhet() {
        Stønadsperiode(stønadsdager = 10) shouldBe Stønadsperiode(stønadsdager = 10)
        Stønadsperiode(Dagpengeperiode(52)) shouldBe Stønadsperiode(Dagpengeperiode(52))
        Stønadsperiode(Dagpengeperiode(52)) shouldBe Stønadsperiode(stønadsdager = 260)

        Stønadsperiode(Dagpengeperiode(51)) shouldNotBe Stønadsperiode(stønadsdager = 260)
        Stønadsperiode(Dagpengeperiode(51)) shouldNotBe Any()
        Stønadsperiode(Dagpengeperiode(51)) shouldNotBe null

        Stønadsperiode(Dagpengeperiode(52)).hashCode() shouldBe Stønadsperiode(Dagpengeperiode(52)).hashCode()
        Stønadsperiode(Dagpengeperiode(52)).hashCode() shouldBe Stønadsperiode(stønadsdager = 260).hashCode()
    }
}
