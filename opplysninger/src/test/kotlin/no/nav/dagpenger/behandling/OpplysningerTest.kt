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

    private object Grunnbeløp {
        const val TEST_GRUNNBELØP = 118620.0

        fun finnFor(dato: LocalDate) = TEST_GRUNNBELØP
    }

    @Test
    fun `test som sjekker minsteinntekt og kravet til alder`() {
        val vilkår = Opplysningstype<Boolean>("Vilkår")
        val regelsett = Regelsett("regelsett")

        val nedreTerskelFaktor = Opplysningstype<Double>("Antall G for krav til 12 mnd inntekt")
        val øvreTerskelFaktor = Opplysningstype<Double>("Antall G for krav til 36 mnd inntekt")
        val inntekt12 = Opplysningstype<Double>("Inntekt siste 12 mnd")
        val inntekt36 = Opplysningstype<Double>("Inntekt siste 36 mnd")
        val grunnbeløp = Opplysningstype<Double>("Grunnbeløp")
        val virkningsdato = Opplysningstype<LocalDate>("Virkningsdato")

        regelsett.oppslag(grunnbeløp, virkningsdato) { Grunnbeløp.finnFor(it) }

        val nedreTerskel = Opplysningstype<Double>("Inntektskrav for siste 12 mnd")
        regelsett.multiplikasjon(nedreTerskel, nedreTerskelFaktor, grunnbeløp)

        val øvreTerskel = Opplysningstype<Double>("Inntektskrav for siste 36 mnd")
        regelsett.multiplikasjon(øvreTerskel, øvreTerskelFaktor, grunnbeløp)

        val overNedreTerskel = Opplysningstype<Boolean>("Inntekt er over kravet for siste 12 mnd")
        regelsett.størreEnnEllerLik(overNedreTerskel, inntekt12, nedreTerskel)

        val overØvreTerskel = Opplysningstype<Boolean>("Inntekt er over kravet for siste 36 mnd")
        regelsett.størreEnnEllerLik(overØvreTerskel, inntekt36, øvreTerskel)

        val minsteinntekt = Opplysningstype("Minsteinntekt", vilkår)
        regelsett.enAv(minsteinntekt, overNedreTerskel, overØvreTerskel)

        val alleVilkår = Opplysningstype<Boolean>("Alle vilkår")
        regelsett.alle(alleVilkår, minsteinntekt, Alderskrav.vilkår)

        val regelverksdato = 10.mai.atStartOfDay()
        val opplysninger =
            Opplysninger(
                listOf(
                    // Setter opp opplysninger med ting som er kjent fra før
                    // Har er ikke lengre gyldig og må hentes på nytt
                    Faktum(inntekt12, 221221.0, Gyldighetsperiode(1.januar, 1.mai)),
                ),
            )
        val regelkjøring = Regelkjøring(regelverksdato, opplysninger, regelsett, Alderskrav.regelsett)

        // Sett virkningsdato som en opplysning
        opplysninger.leggTil(Faktum(virkningsdato, regelverksdato.toLocalDate()))

        // Flyt for å innhente manglende opplysninger
        val actual = regelkjøring.trenger(minsteinntekt)
        val actual2 = regelkjøring.trenger(Alderskrav.vilkår)

        assertEquals(1, actual2.size)
        assertEquals(4, actual.size)
        assertEquals(setOf(inntekt12, inntekt36, nedreTerskelFaktor, øvreTerskelFaktor), actual)

        assertEquals(Grunnbeløp.TEST_GRUNNBELØP, opplysninger.finnOpplysning(grunnbeløp).verdi)

        opplysninger.leggTil(Faktum(nedreTerskelFaktor, 1.5))
        assertEquals(3, regelkjøring.trenger(minsteinntekt).size)

        opplysninger.leggTil(Faktum(øvreTerskelFaktor, 3.0))
        assertEquals(2, regelkjøring.trenger(minsteinntekt).size)

        // Har er ikke lengre gyldig inntekt og må hentes på nytt
        opplysninger.leggTil(Hypotese(inntekt12, 321321.0, Gyldighetsperiode(9.mai)))
        opplysninger.leggTil(Hypotese(inntekt36, 321321.0, Gyldighetsperiode(9.mai, 12.mai)))
        assertEquals(0, regelkjøring.trenger(minsteinntekt).size)

        assertTrue(opplysninger.har(minsteinntekt))
        assertTrue(opplysninger.finnOpplysning(minsteinntekt).verdi)

        // opplysninger.leggTil(Faktum(Alderskrav.virkningsdato, LocalDate.of(2020, 2, 29)))
        opplysninger.leggTil(Faktum(fødselsdato, LocalDate.of(1953, 2, 10)))
        assertTrue(opplysninger.har(alleVilkår))

        val regelDAG = RegeltreBygger(regelsett, Alderskrav.regelsett).dag()
        val mermaidDiagram = MermaidPrinter(regelDAG).toPrint()
        println(mermaidDiagram)
        println(opplysninger.toString())

        val dataDAG = DatatreBygger(opplysninger).dag()
        println(MermaidPrinter(dataDAG, retning = "LR").toPrint())
    }

    @Test
    fun `test av datoer ved å sjekke kravet til alder`() {
        val fraDato = 10.mai.atStartOfDay()
        val opplysninger = Opplysninger()
        val regelkjøring = Regelkjøring(fraDato, opplysninger, regelsett)

        // Flyt for å innhente manglende opplysninger
        val trenger = regelkjøring.trenger(Alderskrav.vilkår)
        // TODO: Aldersgrense burde ikke dukke opp her, vi har jo en regel
        assertEquals(setOf(fødselsdato, virkningsdato, aldersgrense), trenger)

        opplysninger.leggTil(Faktum(virkningsdato, LocalDate.of(2020, 2, 29)))
        assertEquals(setOf(fødselsdato), regelkjøring.trenger(Alderskrav.vilkår))

        opplysninger.leggTil(Faktum(fødselsdato, LocalDate.of(1953, 2, 10)))

        assertTrue(opplysninger.har(Alderskrav.vilkår))
        assertTrue(opplysninger.finnOpplysning(Alderskrav.vilkår).verdi)

        val regelDAG = RegeltreBygger(regelsett).dag()
        val mermaidDiagram = MermaidPrinter(regelDAG).toPrint()
        println(mermaidDiagram)
        println(opplysninger.toString())

        val dataDAG = DatatreBygger(opplysninger).dag()
        println(MermaidPrinter(dataDAG, retning = "LR").toPrint())
    }
}
