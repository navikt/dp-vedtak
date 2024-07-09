package no.nav.dagpenger.dag.printer

import no.nav.dagpenger.dag.TestOpplysningstyper.faktorA
import no.nav.dagpenger.dag.TestOpplysningstyper.faktorB
import no.nav.dagpenger.dag.TestOpplysningstyper.produkt
import no.nav.dagpenger.opplysning.Regelsett
import no.nav.dagpenger.opplysning.dag.RegeltreBygger
import no.nav.dagpenger.opplysning.regel.multiplikasjon
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class DAGPrinterTest {
    private val regelsett = Regelsett("regelsett") { regel(produkt) { multiplikasjon(faktorA, faktorB) } }

    private val regeltre = RegeltreBygger(regelsett.regler()).dag()

    @Test
    fun `test av PrettyPrinter`() {
        val prettyPrinter = PrettyPrinter(regeltre)
        //language=text
        assertEquals(
            expected =
                """
                Resultat: opplysning om Resultat
                  | Multiplikasjon
                  FaktorA: opplysning om FaktorA
                  | Multiplikasjon
                  FaktorB: opplysning om FaktorB
                """.trimIndent(),
            actual = prettyPrinter.toPrint { it.data == produkt },
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
