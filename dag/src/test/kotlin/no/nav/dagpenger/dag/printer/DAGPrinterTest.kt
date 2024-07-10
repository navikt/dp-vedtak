package no.nav.dagpenger.dag.printer

import no.nav.dagpenger.dag.DAG
import no.nav.dagpenger.dag.Edge
import no.nav.dagpenger.dag.Node
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class DAGPrinterTest {
    private val regeltre =
        DAG<String, String>(
            listOf(
                Edge(from = Node("A", "A"), to = Node("C", "C"), edgeName = "Multiplikasjon"),
                Edge(from = Node("B", "B"), to = Node("A", "A"), edgeName = "Multiplikasjon"),
            ),
        )

    @Test
    fun `test av PrettyPrinter`() {
        val prettyPrinter = PrettyPrinter(regeltre)
        //language=text
        assertEquals(
            expected =
                """
                A: A
                  | Multiplikasjon
                  C: C
                """.trimIndent(),
            actual = prettyPrinter.toPrint { it.data == "A" },
        )
    }

    @Test
    fun `test av MermaidPrinter`() {
        val mermaidPrinter = MermaidPrinter(regeltre)
        assertTrue(mermaidPrinter.toPrint().contains("graph RL"))
        assertEquals(
            // language=mermaid
            """
            graph RL
              A["A"] -->|"Multiplikasjon"| B["C"]
              C["B"] -->|"Multiplikasjon"| A["A"]
            """.trimIndent(),
            mermaidPrinter.toPrint(),
        )
    }
}
