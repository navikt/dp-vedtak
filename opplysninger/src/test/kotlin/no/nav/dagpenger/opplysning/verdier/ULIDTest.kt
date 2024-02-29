package no.nav.dagpenger.opplysning.verdier

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.equals.shouldNotBeEqual
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class ULIDTest {
    @Test
    fun `likhet test`() {
        val id1 = "01HQQWF1TYEWQ3ASZ8H9YYZVQ2"
        val id2 = "01HQQWG50VTYSH8SVRBFTZX723"
        val ulid = Ulid(id1)
        ulid shouldBeEqual Ulid(id1)
        Ulid(id1) shouldBeEqual ulid
        ulid shouldBeEqual ulid
        ulid shouldNotBeEqual Ulid(id2)
    }

    @Test
    fun `kan ikke opprette tomme Ulid`() {
        val exception =
            shouldThrow<IllegalArgumentException> {
                Ulid("")
            }
        exception.message shouldBe "ULID krever en 26 tegn lang streng."
    }
}
