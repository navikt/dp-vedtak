package no.nav.dagpenger.behandling

import no.nav.dagpenger.behandling.dag.DatatreBygger
import no.nav.dagpenger.behandling.dag.RegeltreBygger
import no.nav.dagpenger.behandling.dag.printer.MermaidPrinter
import no.nav.dagpenger.behandling.regel.alle
import no.nav.dagpenger.behandling.regel.enAv
import no.nav.dagpenger.behandling.regel.multiplikasjon
import no.nav.dagpenger.behandling.regel.oppslag
import no.nav.dagpenger.behandling.regel.størreEnnEllerLik
import no.nav.dagpenger.behandling.regelsett.Alderskrav
import no.nav.dagpenger.behandling.regelsett.Alderskrav.aldersgrense
import no.nav.dagpenger.behandling.regelsett.Alderskrav.fødselsdato
import no.nav.dagpenger.behandling.regelsett.Alderskrav.regelsett
import no.nav.dagpenger.behandling.regelsett.Alderskrav.virkningsdato
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.LocalDate
import kotlin.test.assertEquals

class OpplysningerTest {
    @Test
    fun `vilkår er avhengig av andre vilkår`() {
        val vilkår = Opplysningstype<Boolean>("Vilkår")
        val minsteinntekt = Opplysningstype("Minsteinntekt", vilkår)
        val alder = Opplysningstype("Alder", vilkår)

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
        val opplysningstype = Opplysningstype<Double>("Type")
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
}
