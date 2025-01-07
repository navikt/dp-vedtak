package no.nav.dagpenger.avklaring

import io.kotest.matchers.shouldBe
import no.nav.dagpenger.avklaring.TestAvklaringer.ArbeidIEØS
import no.nav.dagpenger.avklaring.TestAvklaringer.TestIkke123
import no.nav.dagpenger.opplysning.Saksbehandler
import no.nav.dagpenger.opplysning.Saksbehandlerkilde
import no.nav.dagpenger.uuid.UUIDv7
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class AvklaringTest {
    @Test
    fun `avklaring må avklares`() {
        val avklaring = Avklaring(ArbeidIEØS)
        avklaring.måAvklares() shouldBe true
        avklaring.kvitter(Saksbehandlerkilde(UUIDv7.ny(), Saksbehandler("X123456")), "begrunnelse") shouldBe true
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

    @Test
    fun `endringer sorteres etter tid`() {
        val underBehandling = Avklaring.Endring.UnderBehandling()
        val avklart = Avklaring.Endring.Avklart(avklartAv = Saksbehandlerkilde(UUIDv7.ny(), Saksbehandler("X123456")))
        val avklaring =
            Avklaring(
                id = UUIDv7.ny(),
                ArbeidIEØS,
                historikk =
                    mutableListOf(
                        avklart,
                        underBehandling,
                    ),
            )
        avklaring.endringer.size shouldBe 2
        val sisteEndring =
            avklaring.endringer
                .last()
        sisteEndring.javaClass.simpleName shouldBe "Avklart"
        avklaring.sistEndret shouldBe sisteEndring.endret

        // Avklaring ikke lenger relevant
        avklaring.avbryt() shouldBe true
        avklaring.endringer.size shouldBe 3
        avklaring.endringer
            .last()
            .javaClass.simpleName shouldBe "Avbrutt"
    }
}
