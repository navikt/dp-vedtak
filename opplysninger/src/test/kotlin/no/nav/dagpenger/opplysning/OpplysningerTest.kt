package no.nav.dagpenger.opplysning

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class OpplysningerTest {
    @Test
    fun `vilkår er avhengig av andre vilkår`() {
        val vilkår = Opplysningstype.somBoolsk("Vilkår")
        val minsteinntekt = Opplysningstype.somBoolsk("Minsteinntekt", vilkår)
        val alder = Opplysningstype.somBoolsk("Alder", vilkår)

        val opplysninger = Opplysninger()
        val regelkjøring = Regelkjøring(1.mai, opplysninger)

        opplysninger.leggTil(Faktum(minsteinntekt, true))
        opplysninger.leggTil(Faktum(alder, true))
        opplysninger.leggTil(Faktum(vilkår, true))

        assertTrue(opplysninger.har(minsteinntekt))
        assertTrue(opplysninger.har(alder))
        assertTrue(opplysninger.har(vilkår))
    }

    @Test
    fun `tillater ikke overlappende opplysninger av samme type`() {
        val opplysningstype = Opplysningstype.somDesimaltall("Type")
        val opplysninger = Opplysninger()
        val regelkjøring = Regelkjøring(1.mai, opplysninger)

        opplysninger.leggTil(Faktum(opplysningstype, 0.5, Gyldighetsperiode(1.mai, 10.mai)))
        assertThrows<IllegalArgumentException> {
            opplysninger.leggTil(Faktum(opplysningstype, 0.5))
        }
        opplysninger.leggTil(Faktum(opplysningstype, 1.5, Gyldighetsperiode(11.mai)))

        assertEquals(0.5, opplysninger.finnOpplysning(opplysningstype).verdi)
        assertEquals(0.5, opplysninger.finnOpplysning(opplysningstype).verdi)
        Regelkjøring(15.mai, opplysninger) // Bytt til 15. mai for regelkjøringen
        assertEquals(1.5, opplysninger.finnOpplysning(opplysningstype).verdi)
    }

    @Test
    fun `opplysninger kan erstattes om de gjelder samme periode`() {
        val opplysningstype = Opplysningstype.somDesimaltall("Type")
        val opplysninger = Opplysninger()
        val regelkjøring = Regelkjøring(1.mai, opplysninger)

        val gyldighetsperiode = Gyldighetsperiode(1.mai, 10.mai)
        opplysninger.leggTil(Faktum(opplysningstype, 0.5, gyldighetsperiode))
        opplysninger.leggTil(Faktum(opplysningstype, 1.5, gyldighetsperiode))

        assertEquals(1.5, opplysninger.finnOpplysning(opplysningstype).verdi)
    }

    @Test
    fun `opplysninger kan erstattes om de bare overlapper på halen`() {
        val opplysningstype = Opplysningstype.somDesimaltall("Type")
        val opplysninger = Opplysninger()
        val regelkjøring = Regelkjøring(8.mai, opplysninger)

        opplysninger.leggTil(Faktum(opplysningstype, 0.5, Gyldighetsperiode(1.mai, 10.mai)))
        opplysninger.leggTil(Faktum(opplysningstype, 1.5, Gyldighetsperiode(5.mai, 15.mai)))

        assertEquals(1.5, opplysninger.finnOpplysning(opplysningstype).verdi)
    }

    @Test
    fun `opplysninger kan arve tidligere opplysninger`() {
        val opplysningstype = Opplysningstype.somDesimaltall("Type")
        val tidligereOpplysninger = Opplysninger(listOf(Faktum(opplysningstype, 0.5, Gyldighetsperiode(1.mai, 20.mai))))
        val opplysninger = Opplysninger(tidligereOpplysninger)
        val regelkjøring = Regelkjøring(15.mai, opplysninger)

        // Vi får hentet ut opplysning fra tidligere behandlinger
        assertEquals(0.5, opplysninger.finnOpplysning(opplysningstype).verdi)

        assertThrows<IllegalArgumentException> {
            opplysninger.leggTil(Faktum(opplysningstype, 1.5, Gyldighetsperiode(15.mai, 18.mai)))
        }

        opplysninger.leggTil(Faktum(opplysningstype, 1.5, Gyldighetsperiode(21.mai)))
    }
}
