package no.nav.dagpenger.opplysning

import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.equals.shouldNotBeEqual
import no.nav.dagpenger.opplysning.TestOpplysningstyper.dato1
import no.nav.dagpenger.opplysning.TestOpplysningstyper.foreldrevilkår
import no.nav.dagpenger.opplysning.TestOpplysningstyper.undervilkår1
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class OpplysningstypeTest {
    @Test
    fun `likhet test`() {
        dato1 shouldBeEqual dato1
        dato1.hashCode() shouldBeEqual dato1.hashCode()
        dato1 shouldBeEqual dato1
        dato1 shouldNotBeEqual Any()
    }

    @Test
    fun `enkle opplysningstyper`() {
        assertTrue(dato1.er(dato1))
    }

    @Test
    fun `hierarkiske opplysningstyper`() {
        assertTrue(undervilkår1.er(foreldrevilkår))
        assertTrue(undervilkår1.er(foreldrevilkår))

//        assertEquals(listOf(minsteinntekt), vilkår.bestårAv())
    }
}
