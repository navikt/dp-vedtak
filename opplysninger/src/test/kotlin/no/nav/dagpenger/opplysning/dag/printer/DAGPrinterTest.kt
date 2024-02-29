package no.nav.dagpenger.opplysning.dag.printer

import no.nav.dagpenger.opplysning.Opplysningstype
import no.nav.dagpenger.opplysning.Regelsett
import no.nav.dagpenger.opplysning.dag.RegeltreBygger
import no.nav.dagpenger.opplysning.regel.multiplikasjon
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class DAGPrinterTest {
    private val a = Opplysningstype.somDesimaltall("A")
    private val b = Opplysningstype.somDesimaltall("B")
    private val c = Opplysningstype.somDesimaltall("A * B")
    private val regelsett = Regelsett("regelsett") { regel(c) { multiplikasjon(a, b) } }

    private val regeltre = RegeltreBygger(regelsett.regler()).dag()

    @Test
    fun `test av PrettyPrinter`() {
        val prettyPrinter = PrettyPrinter(regeltre)
        //language=text
        assertEquals(
            expected =
                """
                A * B: opplysning om A * B
                  | Multiplikasjon
                  A: opplysning om A
                  | Multiplikasjon
                  B: opplysning om B
                """.trimIndent(),
            actual = prettyPrinter.toPrint { it.data == c },
        )
    }

    @Test
    fun `test av MermaidPrinter`() {
        val mermaidPrinter = MermaidPrinter(regeltre)
        assertTrue(mermaidPrinter.toPrint().contains("graph RL"))
        /*assertEquals(
        // language=mermaid
            """
            graph RL
              1806431167["A * B"] -->|Multiplikasjon| 1297836716["A"]
              1806431167["A * B"] -->|Multiplikasjon| 710190911["B"]
            """.trimIndent(),
            mermaidPrinter.toPrint(),
        )*/
    }
}
