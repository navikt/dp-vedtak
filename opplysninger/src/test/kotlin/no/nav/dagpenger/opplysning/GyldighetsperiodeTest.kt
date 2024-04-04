package no.nav.dagpenger.opplysning

import io.kotest.matchers.shouldBe
import java.time.LocalDateTime
import java.time.LocalTime
import kotlin.test.Test

internal class GyldighetsperiodeTest {
    @Test
    fun `kan lage gyldighetsperiode`() {
        val gyldighetsperiode = Gyldighetsperiode(1.januar, 10.januar)

        gyldighetsperiode.contains(1.januar.atStartOfDay()) shouldBe true
        gyldighetsperiode.contains(5.januar.atStartOfDay()) shouldBe true
        gyldighetsperiode.contains(LocalDateTime.of(5.januar, LocalTime.MAX)) shouldBe true
        gyldighetsperiode.contains(10.januar.atStartOfDay()) shouldBe true
        gyldighetsperiode.contains(LocalDateTime.of(10.januar, LocalTime.MAX)) shouldBe true
    }
}
