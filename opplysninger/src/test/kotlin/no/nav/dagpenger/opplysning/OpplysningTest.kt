package no.nav.dagpenger.opplysning

import io.kotest.assertions.throwables.shouldNotThrowAny
import io.kotest.matchers.shouldBe
import no.nav.dagpenger.opplysning.TestOpplysningstyper.dato1
import no.nav.dagpenger.opplysning.TestOpplysningstyper.dato2
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

    @Test
    fun `er redigerbar`() {
        val opplysning1 = Faktum(dato1, LocalDate.now())
        val opplysning2 = Faktum(dato2, LocalDate.now())

        opplysning1.kanRedigere(RedigerbarPerOpplysningstype) shouldBe true
        opplysning2.kanRedigere(RedigerbarPerOpplysningstype) shouldBe false
        opplysning2.kanRedigere { true } shouldBe true
    }

    private object RedigerbarPerOpplysningstype : Redigerbar {
        private val redigerbare = setOf(dato1)

        override fun kanRedigere(opplysning: Opplysning<*>): Boolean = redigerbare.contains(opplysning.opplysningstype)
    }
}
