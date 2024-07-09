package no.nav.dagpenger.dag

data class Node<T>(
    val name: String,
    val data: T,
)

data class Edge<T, E>(
    val from: Node<T>,
    val to: Node<T>,
    val edgeName: String,
    val data: E? = null,
)

data class DAG<T, E>(
    val edges: List<Edge<T, E>>,
    private val løseNoder: List<Node<T>> = emptyList(),
) {
    private val _nodes: Set<Node<T>> = edges.flatMap { listOf(it.from, it.to) }.toSet() + løseNoder
    val nodes = _nodes.toList()

    fun findLeafNodes(): List<Node<T>> {
        val outgoingNodes = edges.map { it.from }
        return _nodes.filterNot { it in outgoingNodes }
    }

    fun findNodesWithEdge(block: (Edge<T, E>) -> Boolean): List<Node<T>> {
        val outgoingNodes = edges.filter { block(it) }.map { it.from }
        return _nodes.filter { it in outgoingNodes }
    }

    fun subgraph(block: (T) -> Boolean): DAG<T, E> {
        if (edges.isEmpty()) return DAG(emptyList())

        val subgraphEdges = mutableListOf<Edge<T, E>>()
        val startingNode = _nodes.find { block(it.data) } ?: throw IllegalArgumentException("Starting node not found")

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
