package no.nav.dagpenger.opplysning

import no.nav.dagpenger.opplysning.TestOpplysningstyper.desimaltall
import no.nav.dagpenger.opplysning.TestOpplysningstyper.foreldrevilkår
import no.nav.dagpenger.opplysning.TestOpplysningstyper.undervilkår1
import no.nav.dagpenger.opplysning.TestOpplysningstyper.undervilkår2
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

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
    fun `tillater ikke overlappende opplysninger av samme type`() {
        val opplysninger =
            Opplysninger().also {
                Regelkjøring(1.mai, it)
            }

        opplysninger.leggTil(Faktum(desimaltall, 0.5, Gyldighetsperiode(1.mai, 10.mai)))
        assertThrows<IllegalArgumentException> {
            opplysninger.leggTil(Faktum(desimaltall, 0.5))
        }
        opplysninger.leggTil(Faktum(desimaltall, 1.5, Gyldighetsperiode(11.mai)))

        assertEquals(0.5, opplysninger.finnOpplysning(desimaltall).verdi)
        assertEquals(0.5, opplysninger.finnOpplysning(desimaltall).verdi)
        Regelkjøring(15.mai, opplysninger) // Bytt til 15. mai for regelkjøringen
        assertEquals(1.5, opplysninger.finnOpplysning(desimaltall).verdi)
    }

    @Test
    fun `opplysninger kan erstattes om de gjelder samme periode`() {
        val opplysninger =
            Opplysninger().also {
                Regelkjøring(1.mai, it)
            }

        val gyldighetsperiode = Gyldighetsperiode(1.mai, 10.mai)
        opplysninger.leggTil(Faktum(desimaltall, 0.5, gyldighetsperiode))
        opplysninger.leggTil(Faktum(desimaltall, 1.5, gyldighetsperiode))

        assertEquals(1.5, opplysninger.finnOpplysning(desimaltall).verdi)
    }

    @Test
    fun `opplysninger kan erstattes om de bare overlapper på halen`() {
        val opplysninger =
            Opplysninger().also {
                Regelkjøring(8.mai, it)
            }

        opplysninger.leggTil(Faktum(desimaltall, 0.5, Gyldighetsperiode(1.mai, 10.mai)))
        opplysninger.leggTil(Faktum(desimaltall, 1.5, Gyldighetsperiode(5.mai, 15.mai)))

        assertEquals(1.5, opplysninger.finnOpplysning(desimaltall).verdi)
    }

    @Test
    fun `opplysninger kan arve tidligere opplysninger`() {
        val tidligereOpplysninger = Opplysninger(listOf(Faktum(desimaltall, 0.5, Gyldighetsperiode(1.mai, 20.mai))))
        val opplysninger =
            Opplysninger(tidligereOpplysninger).also {
                Regelkjøring(15.mai, it)
            }

        // Vi får hentet ut opplysning fra tidligere behandlinger
        assertEquals(0.5, opplysninger.finnOpplysning(desimaltall).verdi)

        assertThrows<IllegalArgumentException> {
            opplysninger.leggTil(Faktum(desimaltall, 1.5, Gyldighetsperiode(15.mai, 18.mai)))
        }

        opplysninger.leggTil(Faktum(desimaltall, 1.5, Gyldighetsperiode(21.mai)))
    }
}
