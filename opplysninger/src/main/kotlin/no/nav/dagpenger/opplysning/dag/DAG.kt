package no.nav.dagpenger.opplysning.dag

data class Node<T>(val name: String, val data: T)

data class Edge<T>(val from: Node<T>, val to: Node<T>, val edgeName: String)

data class DAG<T>(internal val edges: List<Edge<T>>, private val løseNoder: List<Node<T>> = emptyList()) {
    internal val nodes: Set<Node<T>> = edges.flatMap { listOf(it.from, it.to) }.toSet() + løseNoder

    fun findLeafNodes(): List<Node<T>> {
        val outgoingNodes = edges.map { it.from }
        return nodes.filterNot { it in outgoingNodes }
    }

    fun findNodesWithEdgeNamed(name: String): List<Node<T>> {
        val outgoingNodes = edges.filter { it.edgeName == name }.map { it.from }
        return nodes.filter { it in outgoingNodes }
    }

    fun subgraph(block: (T) -> Boolean): DAG<T> {
        if (edges.isEmpty()) return DAG(emptyList())

        val subgraphEdges = mutableListOf<Edge<T>>()
        val startingNode = nodes.find { block(it.data) } ?: throw IllegalArgumentException("Starting node not found")

        // Recursive function to traverse the graph and add nodes and edges to the subgraph
        fun traverse(node: Node<T>) {
            val outgoingEdges = edges.filter { it.from == node }
            outgoingEdges.forEach { edge ->
                if (edge in subgraphEdges) return@forEach
                subgraphEdges.add(edge)
                traverse(edge.to)
            }
        }

        // Start traversing from the given node
        traverse(startingNode)

        // Return the created subgraph
        return DAG(subgraphEdges)
    }
}
