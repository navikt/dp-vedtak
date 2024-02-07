package no.nav.dagpenger.behandling

import no.nav.dagpenger.behandling.dag.DatatreBygger
import no.nav.dagpenger.behandling.dag.RegeltreBygger
import no.nav.dagpenger.behandling.dag.printer.MermaidPrinter
import no.nav.dagpenger.behandling.regel.alle
import no.nav.dagpenger.behandling.regelsett.Alderskrav
import no.nav.dagpenger.behandling.regelsett.Minsteinntekt
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.time.LocalDate
import kotlin.test.assertEquals

class RegelmotorIntegrasjonsTest {
    @Test
    fun `som sjekker minsteinntekt og kravet til alder`() {
        val regelverksdato = 10.mai.atStartOfDay()
        val opplysninger =
            Opplysninger(
                listOf(
                    // Setter opp opplysninger med ting som er kjent fra før
                    // Har er ikke lengre gyldig og må hentes på nytt
                    Faktum(Minsteinntekt.inntekt12, 221221.0, Gyldighetsperiode(1.januar, 1.mai)),
                ),
            )
        val alleVilkår = Opplysningstype<Boolean>("Vilkår")
        val regelsett =
            Regelsett("Krav til Dagpenger").apply {
                alle(alleVilkår, Minsteinntekt.minsteinntekt, Alderskrav.vilkår)
            }
        val regelkjøring = Regelkjøring(regelverksdato, opplysninger, regelsett, Alderskrav.regelsett, Minsteinntekt.regelsett)
        // Sett virkningsdato som en opplysning
        opplysninger.leggTil(Faktum(Minsteinntekt.virkningsdato, regelverksdato.toLocalDate()))

        // Flyt for å innhente manglende opplysninger
        val avhengigheterTilMinsteinntekt = regelkjøring.trenger(Minsteinntekt.minsteinntekt)
        val avhengigheterTilAlder = regelkjøring.trenger(Alderskrav.vilkår)

        assertEquals(1, avhengigheterTilAlder.size)
        assertEquals(
            setOf(Alderskrav.fødselsdato),
            avhengigheterTilAlder,
        )
        assertEquals(4, avhengigheterTilMinsteinntekt.size)
        assertEquals(
            setOf(Minsteinntekt.inntekt12, Minsteinntekt.inntekt36, Minsteinntekt.nedreTerskelFaktor, Minsteinntekt.øvreTerskelFaktor),
            avhengigheterTilMinsteinntekt,
        )

        assertEquals(Minsteinntekt.Grunnbeløp.TEST_GRUNNBELØP, opplysninger.finnOpplysning(Minsteinntekt.grunnbeløp).verdi)

        opplysninger.leggTil(Faktum(Minsteinntekt.nedreTerskelFaktor, 1.5))
        assertEquals(3, regelkjøring.trenger(Minsteinntekt.minsteinntekt).size)

        opplysninger.leggTil(Faktum(Minsteinntekt.øvreTerskelFaktor, 3.0))
        assertEquals(2, regelkjøring.trenger(Minsteinntekt.minsteinntekt).size)

        // Har er ikke lengre gyldig inntekt og må hentes på nytt
        opplysninger.leggTil(Hypotese(Minsteinntekt.inntekt12, 321321.0, Gyldighetsperiode(9.mai)))
        opplysninger.leggTil(Hypotese(Minsteinntekt.inntekt36, 321321.0, Gyldighetsperiode(9.mai, 12.mai)))
        assertEquals(0, regelkjøring.trenger(Minsteinntekt.minsteinntekt).size)

        Assertions.assertTrue(opplysninger.har(Minsteinntekt.minsteinntekt))
        Assertions.assertTrue(opplysninger.finnOpplysning(Minsteinntekt.minsteinntekt).verdi)

        // opplysninger.leggTil(Faktum(Alderskrav.virkningsdato, LocalDate.of(2020, 2, 29)))
        opplysninger.leggTil(Faktum(Alderskrav.fødselsdato, LocalDate.of(1953, 2, 10)))
        Assertions.assertTrue(opplysninger.har(alleVilkår))

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
        val regelkjøring = Regelkjøring(fraDato, opplysninger, Alderskrav.regelsett)

        // Flyt for å innhente manglende opplysninger
        val trenger = regelkjøring.trenger(Alderskrav.vilkår)
        // TODO: Aldersgrense burde ikke dukke opp her, vi har jo en regel
        assertEquals(setOf(Alderskrav.fødselsdato, Alderskrav.virkningsdato, Alderskrav.aldersgrense), trenger)

        opplysninger.leggTil(Faktum(Alderskrav.virkningsdato, LocalDate.of(2020, 2, 29)))
        assertEquals(setOf(Alderskrav.fødselsdato), regelkjøring.trenger(Alderskrav.vilkår))

        opplysninger.leggTil(Faktum(Alderskrav.fødselsdato, LocalDate.of(1953, 2, 10)))

        Assertions.assertTrue(opplysninger.har(Alderskrav.vilkår))
        Assertions.assertTrue(opplysninger.finnOpplysning(Alderskrav.vilkår).verdi)

        val regelDAG = RegeltreBygger(Alderskrav.regelsett).dag()
        val mermaidDiagram = MermaidPrinter(regelDAG).toPrint()
        println(mermaidDiagram)
        println(opplysninger.toString())

        val dataDAG = DatatreBygger(opplysninger).dag()
        println(MermaidPrinter(dataDAG, retning = "LR").toPrint())
    }
}
