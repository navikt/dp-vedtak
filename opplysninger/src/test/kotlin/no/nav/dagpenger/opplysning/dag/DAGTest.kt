package no.nav.dagpenger.opplysning.dag

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

class DAGTest {
    private val nodeA = Node("A", "A")
    private val nodeB = Node("B", "B")
    private val dag =
        DAG(
            listOf(nodeA, nodeB),
            listOf(Edge(nodeA, nodeB, "AB")),
        )

    @Test
    fun findLeafNodes() {
        val leafNodes = dag.findLeafNodes()
        assertTrue(leafNodes.size == 1)
        assertEquals(nodeB, leafNodes.first())
    }

    @Test
    @Disabled
    fun subgraph() {
        val subgraph = dag.subgraph { it == "A" }
        assertEquals(1, subgraph.nodes.size)
        assertEquals(0, subgraph.edges.size)
    }
}
