package no.nav.dagpenger.opplysning

import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.equals.shouldNotBeEqual
import no.nav.dagpenger.opplysning.Opplysningstype.Id
import no.nav.dagpenger.opplysning.TestOpplysningstyper.dato1
import no.nav.dagpenger.opplysning.TestOpplysningstyper.dato2
import no.nav.dagpenger.uuid.UUIDv7
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class OpplysningstypeTest {
    @Test
    fun `likhet test`() {
        dato1 shouldBeEqual dato1
        dato1 shouldBeEqual Opplysningstype.dato(Id(UUIDv7.ny(), Dato), "dato1")
        dato1 shouldNotBeEqual Opplysningstype.dato(Id(UUIDv7.ny(), Dato), "dato78")
        dato1.hashCode() shouldBeEqual dato1.hashCode()
        dato1 shouldBeEqual dato1
        dato1 shouldNotBeEqual Any()
        listOf(dato1, dato2) shouldNotBeEqual listOf(dato2, dato1)
        listOf(dato1, dato2).toSet() shouldBeEqual listOf(dato2, dato1).toSet()
    }

    @Test
    fun `enkle opplysningstyper`() {
        assertTrue(dato1.er(dato1))
    }
}
