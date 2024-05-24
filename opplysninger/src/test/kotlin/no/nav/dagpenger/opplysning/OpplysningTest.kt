package no.nav.dagpenger.opplysning

import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.matchers.shouldBe
import no.nav.dagpenger.opplysning.TestOpplysningstyper.dato1
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.UUID

class OpplysningTest {
    @Test
    fun `Har opplysningstype`() {
        val opplysning = Faktum(dato1, LocalDate.now())
        assertTrue(opplysning.er(dato1))
    }

    @Test
    fun `opplysning har uuid versjon 7 id`() {
        val opplysning = Faktum(dato1, LocalDate.now())
        shouldNotThrowAny { UUID.fromString(opplysning.id.toString()) }
        opplysning.id.version() shouldBe 7
    }

    @Test
    fun `opplysning har opprettet dato`() {
        val opplysning = Faktum(dato1, LocalDate.now())
        assertTrue(opplysning.opprettet.isBefore(LocalDateTime.now()))
    }
}
