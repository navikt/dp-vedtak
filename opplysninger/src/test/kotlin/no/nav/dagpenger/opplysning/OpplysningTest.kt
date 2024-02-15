package no.nav.dagpenger.opplysning

import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.util.UUID

class OpplysningTest {
    @Test
    fun `Har opplysningstype`() {
        val opplysning = Faktum(Opplysningstype.somDato("Fødselsdato"), LocalDate.now())
        assertTrue(opplysning.er(Opplysningstype.somDato("Fødselsdato")))
    }

    @Test
    fun `opplysning har uuid versjon 7 id`() {
        val opplysning = Faktum(Opplysningstype.somDato("Fødselsdato"), LocalDate.now())
        shouldNotThrowAny { UUID.fromString(opplysning.id.toString()) }
        opplysning.id.version() shouldBe 7
    }
}
