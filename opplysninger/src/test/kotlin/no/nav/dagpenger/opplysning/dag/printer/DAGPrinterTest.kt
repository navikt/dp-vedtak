package no.nav.dagpenger.opplysning.dag.printer

import no.nav.dagpenger.opplysning.Regelsett
import no.nav.dagpenger.opplysning.TestOpplysningstyper.faktorA
import no.nav.dagpenger.opplysning.TestOpplysningstyper.faktorB
import no.nav.dagpenger.opplysning.TestOpplysningstyper.produserer
import no.nav.dagpenger.opplysning.dag.RegeltreBygger
import no.nav.dagpenger.opplysning.regel.multiplikasjon
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class DAGPrinterTest {
    private val regelsett = Regelsett("regelsett") { regel(produserer) { multiplikasjon(faktorA, faktorB) } }

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
            actual = prettyPrinter.toPrint { it.data == produserer },
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
