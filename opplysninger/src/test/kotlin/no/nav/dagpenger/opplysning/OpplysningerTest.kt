package no.nav.dagpenger.opplysning

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import no.nav.dagpenger.opplysning.TestOpplysningstyper.desimaltall
import no.nav.dagpenger.opplysning.TestOpplysningstyper.foreldrevilkår
import no.nav.dagpenger.opplysning.TestOpplysningstyper.undervilkår1
import no.nav.dagpenger.opplysning.TestOpplysningstyper.undervilkår2
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

@Disabled
class OpplysningerTest {
    @Test
    fun `vilkår er avhengig av andre vilkår`() {
        val opplysninger =
            Opplysninger().also {
                Regelkjøring(1.mai, it)
            }

        opplysninger.leggTil(Faktum(undervilkår1, true))
        opplysninger.leggTil(Faktum(undervilkår2, true))
        opplysninger.leggTil(Faktum(foreldrevilkår, true))

        assertTrue(opplysninger.har(undervilkår1))
        assertTrue(opplysninger.har(undervilkår2))
        assertTrue(opplysninger.har(foreldrevilkår))
    }

    @Test
    fun `Ny opplysning overlapper på halen av eksisterende opplysning`() {
        // Og skal endre tilOgMed for eksisterende opplysning
        // Og skal legge til ny opplysning
        val opplysninger = Opplysninger()

        val original = Faktum(desimaltall, 0.5, Gyldighetsperiode(fom = 1.mai))
        val nyOpplysning = Faktum(desimaltall, 1.5, Gyldighetsperiode(fom = 11.mai))
        opplysninger.leggTil(original)
        opplysninger.leggTil(nyOpplysning)

        assertEquals(0.5, opplysninger.forDato(10.mai).finnOpplysning(desimaltall).verdi)
        assertEquals(1.5, opplysninger.forDato(15.mai).finnOpplysning(desimaltall).verdi)

        opplysninger.forDato(5.mai).finnOpplysning(desimaltall).erstatter shouldBe original
        opplysninger.forDato(15.mai).finnOpplysning(desimaltall).erstatter shouldBe null
    }

    @Test
    fun `Ny opplysning overlapper samme periode`() {
        // Da skal vi erstatte gammel opplysning med ny
        val opplysninger = Opplysninger()

        val original = Faktum(desimaltall, 0.5, Gyldighetsperiode(fom = 1.mai))
        val nyOpplysning = Faktum(desimaltall, 1.5, Gyldighetsperiode(fom = 1.mai))
        opplysninger.leggTil(original)
        opplysninger.leggTil(nyOpplysning)

        assertEquals(1.5, opplysninger.forDato(1.mai).finnOpplysning(desimaltall).verdi)
        assertEquals(1.5, opplysninger.forDato(15.mai).finnOpplysning(desimaltall).verdi)

        opplysninger.forDato(5.mai).finnOpplysning(desimaltall).erstatter shouldBe original
    }

    @Test
    fun `Ny opplysning overlapper på starten av eksisterende opplysning`() {
        // Da skal vi erstatte gammel opplysning med ny
        val opplysninger = Opplysninger()

        val original = Faktum(desimaltall, 0.5, Gyldighetsperiode(fom = 5.mai, tom = 20.mai))
        val nyOpplysning = Faktum(desimaltall, 1.5, Gyldighetsperiode(fom = 1.mai, tom = 16.mai))
        opplysninger.leggTil(original)
        opplysninger.leggTil(nyOpplysning)

        assertEquals(1.5, opplysninger.forDato(1.mai).finnOpplysning(desimaltall).verdi)
        assertEquals(1.5, opplysninger.forDato(15.mai).finnOpplysning(desimaltall).verdi)

        shouldThrow<Exception> {
            assertEquals(1.5, opplysninger.forDato(18.mai).finnOpplysning(desimaltall).verdi)
        }

        opplysninger.forDato(5.mai).finnOpplysning(desimaltall).erstatter shouldBe original
    }

    @Test
    fun `Ny opplysning overlapper på midten av eksisterende opplysning`() {
        // Da skal vi erstatte gammel opplysning med forkortet opplysning og legge til ny opplysning
        val opplysninger = Opplysninger()

        val original = Faktum(desimaltall, 0.5, Gyldighetsperiode(fom = 1.mai, tom = 30.mai))
        val nyOpplysning = Faktum(desimaltall, 1.5, Gyldighetsperiode(fom = 10.mai, tom = 20.mai))
        opplysninger.leggTil(original)
        opplysninger.leggTil(nyOpplysning)

        assertEquals(0.5, opplysninger.forDato(1.mai).finnOpplysning(desimaltall).verdi)
        assertEquals(1.5, opplysninger.forDato(15.mai).finnOpplysning(desimaltall).verdi)

        shouldThrow<Exception> {
            assertEquals(1.5, opplysninger.forDato(21.mai).finnOpplysning(desimaltall).verdi)
        }

        opplysninger.forDato(5.mai).finnOpplysning(desimaltall).erstatter shouldBe original
        opplysninger.forDato(15.mai).finnOpplysning(desimaltall).erstatter shouldBe null
    }
}
