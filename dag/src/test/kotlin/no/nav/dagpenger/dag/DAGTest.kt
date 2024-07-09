package no.nav.dagpenger.dag

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class DAGTest {
    private val nodeA = Node("A", "A")
    private val nodeB = Node("B", "B")
    private val nodeC = Node("C", "C")
    private val dag =
        DAG<String, Any?>(
            listOf(
                Edge(nodeC, nodeA, "A to C"),
                Edge(nodeC, nodeB, "B to C"),
            ),
        )

    @Test
    fun findLeafNodes() {
        val leafNodes = dag.findLeafNodes()
        assertEquals(2, leafNodes.size)
        assertEquals(listOf(nodeA, nodeB), leafNodes)
    }

    @Test
    fun subgraph() {
        val subgraph = dag.subgraph { it == "C" }
        assertEquals(3, subgraph.nodes.size)
        assertEquals(2, subgraph.edges.size)
    }
}
