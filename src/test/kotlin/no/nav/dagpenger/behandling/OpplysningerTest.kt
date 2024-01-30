package no.nav.dagpenger.behandling

import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class OpplysningerTest {
    @Test
    fun `vilkår er avhengig av andre vilkår`() {
        val vilkår = Opplysningstype<Boolean>("Vilkår")
        val minsteinntekt = Opplysningstype("Minsteinntekt", vilkår)
        val alder = Opplysningstype("Alder", vilkår)

        val opplysninger = Opplysninger(Regelmotor())

        opplysninger.leggTil(Faktum(minsteinntekt, true))
        opplysninger.leggTil(Faktum(alder, true))
        opplysninger.leggTil(Faktum(vilkår, true))

        assertEquals(3, opplysninger.size)
        assertTrue(opplysninger.har(minsteinntekt))
        assertTrue(opplysninger.har(alder))
    }

    @Test
    fun `finn alle løvnoder som mangler`() {
        val vilkår = Opplysningstype<Boolean>("Vilkår")

        val nedreTerskelFaktor = Opplysningstype<Double>("Nedre inntektsterskel")
        val øvreTerskelFaktor = Opplysningstype<Double>("Øvre inntektsterskel")
        val inntekt = Opplysningstype<Double>("Inntekt")
        val grunnbeløp = Opplysningstype<Double>("Grunnbeløp")

        val regelmotor = Regelmotor()

        // Disse bør få utledesAv fra reglene
        val nedreTerskel = Opplysningstype<Double>("Nedre terskel")
        regelmotor.multiplikasjon(nedreTerskel, nedreTerskelFaktor, grunnbeløp)

        val øvreTerskel = Opplysningstype<Double>("Øvre terskel")
        regelmotor.multiplikasjon(øvreTerskel, øvreTerskelFaktor, grunnbeløp)

        val overNedreTerskel = Opplysningstype<Boolean>("Over nedre terskel")
        regelmotor.størreEnn(overNedreTerskel, inntekt, nedreTerskel)

        val overØvreTerskel = Opplysningstype<Boolean>("Over øvre terskel")
        regelmotor.størreEnn(overØvreTerskel, inntekt, øvreTerskel)

        val minsteinntekt = Opplysningstype("Minsteinntekt", vilkår)
        regelmotor.enAvRegel(minsteinntekt, overNedreTerskel, overØvreTerskel)

        val opplysninger = Opplysninger(regelmotor)

        // Finn alle opplysninger som ikke kan utledes (har ikke andre avhengigheter) og mangler
        val actual = opplysninger.trenger(minsteinntekt)
        assertEquals(4, actual.size)
        assertEquals(listOf(inntekt, nedreTerskelFaktor, grunnbeløp, øvreTerskelFaktor), actual)

        opplysninger.leggTil(Faktum(nedreTerskelFaktor, 1.5))
        assertEquals(3, opplysninger.trenger(minsteinntekt).size)

        opplysninger.leggTil(Faktum(øvreTerskelFaktor, 3.0))
        assertEquals(2, opplysninger.trenger(minsteinntekt).size)

        opplysninger.leggTil(Faktum(inntekt, 321321.0))
        assertEquals(1, opplysninger.trenger(minsteinntekt).size)

        opplysninger.leggTil(Faktum(grunnbeløp, 123123.0))
        assertEquals(0, opplysninger.trenger(minsteinntekt).size)

        assertTrue(opplysninger.har(minsteinntekt))
        assertTrue(opplysninger.find { it.er(minsteinntekt) }?.verdi as Boolean)
    }
}
