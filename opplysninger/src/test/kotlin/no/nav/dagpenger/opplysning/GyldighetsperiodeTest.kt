package no.nav.dagpenger.opplysning

import io.kotest.matchers.shouldBe
import kotlin.test.Test

internal class GyldighetsperiodeTest {
    @Test
    fun `kan lage gyldighetsperiode`() {
        val gyldighetsperiode = Gyldighetsperiode(1.januar, 10.januar)

        gyldighetsperiode.contains(1.januar) shouldBe true
        gyldighetsperiode.contains(5.januar) shouldBe true
        gyldighetsperiode.contains(10.januar) shouldBe true
    }
}
