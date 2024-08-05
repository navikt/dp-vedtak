package no.nav.dagpenger.opplysning

import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.maps.shouldContainAll
import no.nav.dagpenger.dag.printer.MermaidPrinter
import no.nav.dagpenger.opplysning.dag.DatatreBygger
import no.nav.dagpenger.opplysning.dag.RegeltreBygger
import no.nav.dagpenger.opplysning.regel.alle
import no.nav.dagpenger.opplysning.regel.innhentMed
import no.nav.dagpenger.opplysning.regelsett.Alderskrav
import no.nav.dagpenger.opplysning.regelsett.Grunnbeløp
import no.nav.dagpenger.opplysning.regelsett.ReglerForInntektTest
import no.nav.dagpenger.opplysning.regelsett.Virkningsdato
import no.nav.dagpenger.opplysning.regelsett.Virkningsdato.virkningsdato
import no.nav.dagpenger.opplysning.verdier.Beløp
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import java.time.LocalDate
import kotlin.test.assertEquals

class RegelmotorIntegrasjonsTest {
    @Test
    fun `som sjekker minsteinntekt og kravet til alder`() {
        val regelverksdato = 10.mai
        val tidligereBehandling =
            Opplysninger(
                listOf(
                    // Setter opp opplysninger med ting som er kjent fra før
                    // Har er ikke lengre gyldig og må hentes på nytt
                    Faktum(ReglerForInntektTest.inntekt12, Beløp(221221.0), Gyldighetsperiode(1.januar, 1.mai)),
                ),
            )
        val opplysninger = Opplysninger(tidligereBehandling)
        val alleVilkår = Opplysningstype.somBoolsk("Vilkår")
        val regelsett =
            Regelsett("Krav til Dagpenger").apply {
                regel(alleVilkår) { alle(ReglerForInntektTest.minsteinntekt, Alderskrav.vilkår) }
            }
        val regelkjøring =
            Regelkjøring(
                regelverksdato,
                opplysninger,
                regelsett,
                Virkningsdato.regelsett,
                Alderskrav.regelsett,
                ReglerForInntektTest.regelsett,
            )

        // Sett virkningsdato som en opplysning
        opplysninger.leggTil(
            Faktum(
                Virkningsdato.søknadsdato,
                regelverksdato,
                Gyldighetsperiode(regelverksdato),
            ),
        )
        opplysninger.leggTil(Faktum(Virkningsdato.sisteDagMedArbeidsplikt, regelverksdato))
        opplysninger.leggTil(Faktum(Virkningsdato.sisteDagMedLønn, regelverksdato))

        // Flyt for å innhente manglende opplysninger
        val avhengigheterTilalleVilkår = regelkjøring.trenger(alleVilkår)
        val avhengigheterTilMinsteinntekt = regelkjøring.trenger(ReglerForInntektTest.minsteinntekt)
        val avhengigheterTilAlder = regelkjøring.trenger(Alderskrav.vilkår)

        val forventetAlderskravOpplysninger = setOf(Alderskrav.fødselsdato)
        val forventetMinsteinntektOpplysninger =
            setOf(
                ReglerForInntektTest.inntekt12,
                ReglerForInntektTest.inntekt36,
                Alderskrav.fødselsdato,
            )
        avhengigheterTilAlder shouldContainExactly forventetAlderskravOpplysninger
        avhengigheterTilMinsteinntekt shouldContainExactly forventetMinsteinntektOpplysninger
        avhengigheterTilalleVilkår shouldContainExactly (forventetAlderskravOpplysninger + forventetMinsteinntektOpplysninger)

        regelkjøring.informasjonsbehov(alleVilkår) shouldContainAll mapOf(Alderskrav.fødselsdato to listOf())
        opplysninger.leggTil(Faktum(Alderskrav.fødselsdato, LocalDate.of(1953, 2, 10)))

        val faktiskVirkningsdato = opplysninger.finnOpplysning(virkningsdato)
        with(regelkjøring.informasjonsbehov(alleVilkår)) {
            shouldContainAll(
                mapOf(
                    ReglerForInntektTest.inntekt12 to listOf(faktiskVirkningsdato),
                    ReglerForInntektTest.inntekt36 to listOf(faktiskVirkningsdato),
                ),
            )
        }
        assertEquals(Grunnbeløp.TEST_GRUNNBELØP, opplysninger.finnOpplysning(ReglerForInntektTest.grunnbeløp).verdi)

        // Har er ikke lengre gyldig inntekt og må hentes på nytt
        opplysninger.leggTil(
            Hypotese(
                ReglerForInntektTest.inntekt12,
                Beløp(321321.0),
                Gyldighetsperiode(9.mai),
                utledetAv = Utledning(ReglerForInntektTest.inntekt12.innhentMed(virkningsdato), listOf(faktiskVirkningsdato)),
            ),
        )
        opplysninger.leggTil(Hypotese(ReglerForInntektTest.inntekt36, Beløp(321321.0), Gyldighetsperiode(9.mai, 12.mai)))
        assertEquals(0, regelkjøring.trenger(ReglerForInntektTest.minsteinntekt).size)

        Assertions.assertTrue(opplysninger.har(ReglerForInntektTest.minsteinntekt))
        Assertions.assertTrue(opplysninger.finnOpplysning(ReglerForInntektTest.minsteinntekt).verdi)

        Assertions.assertTrue(opplysninger.har(alleVilkår))

        val regelDAG = RegeltreBygger(regelsett, ReglerForInntektTest.regelsett, Virkningsdato.regelsett, Alderskrav.regelsett).dag()
        val mermaidDiagram = MermaidPrinter(regelDAG).toPrint()
        println(mermaidDiagram)

        val dataDAG = DatatreBygger(opplysninger).dag()
        println(MermaidPrinter(dataDAG, retning = "LR").toPrint())
    }

    @Test
    fun `test av datoer ved å sjekke kravet til alder`() {
        val fraDato = 10.mai
        val opplysninger = Opplysninger()
        val regelkjøring = Regelkjøring(fraDato, opplysninger, Alderskrav.regelsett)

        // Flyt for å innhente manglende opplysninger
        val trenger = regelkjøring.trenger(Alderskrav.vilkår)
        assertEquals(setOf(Alderskrav.fødselsdato, virkningsdato), trenger)

        opplysninger.leggTil(Faktum(virkningsdato, LocalDate.of(2020, 2, 29)))
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
