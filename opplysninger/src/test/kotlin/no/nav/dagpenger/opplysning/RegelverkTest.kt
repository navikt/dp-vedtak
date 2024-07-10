package no.nav.dagpenger.opplysning

import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.shouldBe
import no.nav.dagpenger.dag.printer.MermaidPrinter
import no.nav.dagpenger.opplysning.regel.erSann
import no.nav.dagpenger.opplysning.regel.innhentMed
import no.nav.dagpenger.opplysning.regel.innhentes
import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.Test

class RegelverkTest {
    private val typeA = Opplysningstype.somBoolsk("A")
    private val typeB = Opplysningstype.somBoolsk("B")
    private val typeC = Opplysningstype.somBoolsk("C")
    private val typeE = Opplysningstype.somBoolsk("E")
    private val typeF = Opplysningstype.somBoolsk("F")

    private val r1 =
        Regelsett("Søknadstidspunkt") {
            regel(typeA) { innhentes }
            regel(typeA, 1.juli) { innhentes }
            regel(typeA, 1.mai) { innhentes }
            regel(typeE) { erSann(typeF) }
            regel(typeE, 1.juni) { erSann(typeF) }
        }
    private val r2 = Regelsett("Inntekt") { regel(typeB) { innhentMed(typeA) } }
    private val r3 = Regelsett("Dagpenger") { regel(typeC) { innhentMed(typeA, typeB) } }

    private val regelverk = Regelverk(r1, r2, r3)

    @Test
    fun `finner alle nødvendige regler for en opplysning helt til venstre`() {
        regelverk.reglerFor(typeA).shouldContainExactlyInAnyOrder(r1.regler())
        regelverk.reglerFor(typeA, 5.mai).shouldContainExactlyInAnyOrder(r1.regler(5.mai))

        regelverk.regelsettFor(typeA).shouldContainExactlyInAnyOrder(r1)
    }

    @Test
    fun `finner alle nødvendige regler for en opplysning i midten`() {
        regelverk.reglerFor(typeB).shouldContainExactlyInAnyOrder(r1.regler() + r2.regler())
        regelverk.reglerFor(typeB, 5.mai).shouldContainExactlyInAnyOrder(r1.regler(5.mai) + r2.regler(5.mai))

        regelverk.regelsettFor(typeB).shouldContainExactlyInAnyOrder(r1, r2)
    }

    @Test
    fun `finner alle nødvendige regler for en opplysning helt til høyre`() {
        regelverk.reglerFor(typeC).shouldContainExactlyInAnyOrder(r1.regler() + r2.regler() + r3.regler())
        regelverk.reglerFor(typeC, 5.mai).shouldContainExactlyInAnyOrder(r1.regler(5.mai) + r2.regler(5.mai) + r3.regler(5.mai))

        regelverk.regelsettFor(typeC).shouldContainExactlyInAnyOrder(r1, r2, r3)
    }

    @Test
    fun `kan også vise regelverket som ett tre`() {
        val dag = regelverk.regeltreFor(typeC)

        MermaidPrinter(dag).also {
            it.toPrint() shouldBe forventedDiagram
        }
    }

    @Language("Mermaid")
    private val forventedDiagram =
        """
        |graph RL
        |  A["Dagpenger"] -->|"avhenger av"| B["Søknadstidspunkt"]
        |  A["Dagpenger"] -->|"avhenger av"| C["Inntekt"]
        |  C["Inntekt"] -->|"avhenger av"| B["Søknadstidspunkt"]
        """.trimMargin()
}
