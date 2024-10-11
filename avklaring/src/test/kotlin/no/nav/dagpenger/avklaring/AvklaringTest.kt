package no.nav.dagpenger.avklaring

import io.kotest.matchers.shouldBe
import no.nav.dagpenger.avklaring.TestAvklaringer.ArbeidIEØS
import no.nav.dagpenger.avklaring.TestAvklaringer.TestIkke123
import no.nav.dagpenger.opplysning.Saksbehandlerkilde
import no.nav.dagpenger.uuid.UUIDv7
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class AvklaringTest {
    @Test
    fun `avklaring må avklares`() {
        val avklaring = Avklaring(ArbeidIEØS)
        avklaring.måAvklares() shouldBe true
        avklaring.kvittering(Saksbehandlerkilde(UUIDv7.ny(), "Z123456")) shouldBe true
        avklaring.måAvklares() shouldBe false
    }

    @Test
    fun `avklaring i set er unike per kode`() {
        val avklaringer =
            setOf(
                Avklaring(ArbeidIEØS),
                Avklaring(ArbeidIEØS),
                Avklaring(TestIkke123),
                Avklaring(TestIkke123),
            )

        assertEquals(2, avklaringer.size)
    }
}
