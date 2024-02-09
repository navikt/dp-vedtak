package no.nav.dagpenger.opplysning.dag

data class Node<T>(val name: String, val data: T)

data class Edge<T>(val from: Node<T>, val to: Node<T>, val edgeName: String)

data class DAG<T>(val nodes: List<Node<T>>, val edges: List<Edge<T>>) {
    fun findLeafNodes(): List<Node<T>> {
        val outgoingNodes = edges.map { it.from }
        return nodes.filterNot { it in outgoingNodes }
    }

    fun subgraph(block: (T) -> Boolean): DAG<T> {
        if (nodes.isEmpty()) return DAG(emptyList(), emptyList())

        val subgraphNodes = mutableListOf<Node<T>>()
        val subgraphEdges = mutableListOf<Edge<T>>()

        val startingNode = nodes.find { block(it.data) } ?: throw IllegalArgumentException("Starting node not found")
        // Add the starting node to the subgraph
        subgraphNodes.add(startingNode)

        // Recursive function to traverse the graph and add nodes and edges to the subgraph
        fun traverse(node: Node<T>) {
            val outgoingEdges = edges.filter { it.from == node }
            for (edge in outgoingEdges) {
                val toNode = edge.to
                if (toNode !in subgraphNodes) {
                    // Add the node and edge to the subgraph
                    subgraphNodes.add(toNode)
                    subgraphEdges.add(edge)

                    // Recursively traverse the connected nodes
                    traverse(toNode)
                }
            }
        }

        // Start traversing from the given node
        traverse(startingNode)

        // Return the created subgraph
        return DAG(subgraphNodes, subgraphEdges)
    }
}
