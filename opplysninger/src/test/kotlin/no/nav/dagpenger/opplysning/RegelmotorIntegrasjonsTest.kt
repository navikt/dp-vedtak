package no.nav.dagpenger.opplysning

import io.kotest.matchers.maps.shouldContainAll
import io.kotest.matchers.shouldBe
import no.nav.dagpenger.dag.printer.MermaidPrinter
import no.nav.dagpenger.opplysning.dag.DatatreBygger
import no.nav.dagpenger.opplysning.dag.RegeltreBygger
import no.nav.dagpenger.opplysning.regel.alle
import no.nav.dagpenger.opplysning.regel.innhentMed
import no.nav.dagpenger.opplysning.regel.innhentes
import no.nav.dagpenger.opplysning.regelsett.Alderskrav
import no.nav.dagpenger.opplysning.regelsett.Alderskrav.fødselsdato
import no.nav.dagpenger.opplysning.regelsett.Grunnbeløp
import no.nav.dagpenger.opplysning.regelsett.ReglerForInntektTest
import no.nav.dagpenger.opplysning.regelsett.Virkningsdato
import no.nav.dagpenger.opplysning.regelsett.Virkningsdato.sisteDagMedArbeidsplikt
import no.nav.dagpenger.opplysning.regelsett.Virkningsdato.sisteDagMedLønn
import no.nav.dagpenger.opplysning.regelsett.Virkningsdato.søknadsdato
import no.nav.dagpenger.opplysning.regelsett.Virkningsdato.virkningsdato
import no.nav.dagpenger.opplysning.verdier.Beløp
import org.junit.jupiter.api.Assertions.assertTrue
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
        opplysninger
            .leggTil(
                Faktum(
                    søknadsdato,
                    regelverksdato,
                    Gyldighetsperiode(regelverksdato),
                ),
            ).also { regelkjøring.evaluer() }
        opplysninger.leggTil(Faktum(sisteDagMedArbeidsplikt, regelverksdato)).also { regelkjøring.evaluer() }
        opplysninger.leggTil(Faktum(sisteDagMedLønn, regelverksdato)).also { regelkjøring.evaluer() }

        regelkjøring.evaluer().informasjonsbehov shouldContainAll mapOf(fødselsdato to listOf())
        opplysninger.leggTil(Faktum(fødselsdato, LocalDate.of(1953, 2, 10))).also { regelkjøring.evaluer() }

        val faktiskVirkningsdato = opplysninger.finnOpplysning(virkningsdato)
        with(regelkjøring.evaluer().informasjonsbehov) {
            shouldContainAll(
                mapOf(
                    ReglerForInntektTest.inntekt12 to listOf(faktiskVirkningsdato),
                    ReglerForInntektTest.inntekt36 to listOf(faktiskVirkningsdato),
                ),
            )
        }
        assertEquals(Grunnbeløp.TEST_GRUNNBELØP, opplysninger.finnOpplysning(ReglerForInntektTest.grunnbeløp).verdi)

        // Har er ikke lengre gyldig inntekt og må hentes på nytt
        opplysninger
            .leggTil(
                Hypotese(
                    ReglerForInntektTest.inntekt12,
                    Beløp(321321.0),
                    Gyldighetsperiode(9.mai),
                    utledetAv = Utledning(ReglerForInntektTest.inntekt12.innhentMed(virkningsdato), listOf(faktiskVirkningsdato)),
                ),
            ).also { regelkjøring.evaluer() }
        opplysninger
            .leggTil(Hypotese(ReglerForInntektTest.inntekt36, Beløp(321321.0), Gyldighetsperiode(9.mai, 12.mai)))
            .also { regelkjøring.evaluer() }
        assertEquals(0, regelkjøring.evaluer().mangler.size)

        assertTrue(opplysninger.har(ReglerForInntektTest.minsteinntekt))
        assertTrue(opplysninger.finnOpplysning(ReglerForInntektTest.minsteinntekt).verdi)

        assertTrue(opplysninger.har(alleVilkår))

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
        val regelkjøring = Regelkjøring(fraDato, opplysninger, TestProsess())

        // Flyt for å innhente manglende opplysninger
        val mangler = regelkjøring.evaluer().mangler
        assertEquals(setOf(fødselsdato, søknadsdato, sisteDagMedArbeidsplikt, sisteDagMedLønn), mangler)

        // Skal kortslutte behovet for de tre underliggende opplysningene
        opplysninger.leggTil(Faktum(virkningsdato, LocalDate.of(2020, 2, 29))).also { regelkjøring.evaluer() }
        assertEquals(setOf(fødselsdato), regelkjøring.evaluer().mangler)

        opplysninger.leggTil(Faktum(fødselsdato, LocalDate.of(1953, 2, 10))).also { regelkjøring.evaluer() }

        assertTrue(opplysninger.har(Alderskrav.vilkår))
        assertTrue(opplysninger.finnOpplysning(Alderskrav.vilkår).verdi)

        val regelDAG = RegeltreBygger(Alderskrav.regelsett).dag()
        val mermaidDiagram = MermaidPrinter(regelDAG).toPrint()
        println(mermaidDiagram)
        println(opplysninger.toString())

        val dataDAG = DatatreBygger(opplysninger).dag()
        println(MermaidPrinter(dataDAG, retning = "LR").toPrint())
    }

    @Test
    fun `asdf`() {
        val fraDato = 10.mai
        val opplysninger = Opplysninger()
        val a0 = Opplysningstype.somBoolsk("A0")
        val a = Opplysningstype.somBoolsk("A")
        val b = Opplysningstype.somBoolsk("B")
        val c = Opplysningstype.somBoolsk("C")
        val d = Opplysningstype.somBoolsk("D")
        val regelsett =
            Regelsett("test") {
                regel(a0) { innhentes }
                regel(a) { alle(a0) }
                regel(d) { innhentes }
                regel(b) { alle(a, d) }
                regel(c) { alle(b) }
            }
        val regelkjøring =
            Regelkjøring(
                fraDato,
                opplysninger,
                object : Forretningsprosess {
                    override fun regelsett(): List<Regelsett> = listOf(regelsett)

                    override fun ønsketResultat(opplysninger: LesbarOpplysninger) = listOf(c)
                },
            )

        opplysninger.leggTil(Faktum(a0, true)).also { regelkjøring.evaluer() }
        opplysninger.leggTil(Faktum(d, true)).also { regelkjøring.evaluer() }

        opplysninger.har(c) shouldBe true
        opplysninger.finnOpplysning(c).verdi shouldBe true

        opplysninger.leggTil(Faktum(a0, false)).also { regelkjøring.evaluer() }

        opplysninger.finnOpplysning(c).verdi shouldBe false
    }
}

private class TestProsess : Forretningsprosess {
    override fun regelsett(): List<Regelsett> = listOf(Alderskrav.regelsett, Virkningsdato.regelsett)

    override fun ønsketResultat(opplysninger: LesbarOpplysninger): List<Opplysningstype<*>> = listOf(Alderskrav.vilkår, virkningsdato)
}
