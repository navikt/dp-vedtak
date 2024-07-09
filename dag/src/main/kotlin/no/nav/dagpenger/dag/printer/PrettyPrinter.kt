package no.nav.dagpenger.dag.printer

import no.nav.dagpenger.dag.DAG
import no.nav.dagpenger.dag.Node

class PrettyPrinter(
    private val dag: DAG<*, Any?>,
) : DAGPrinter {
    override fun toPrint(block: RootNodeFinner?): String {
        val adjacencyList = dag.edges.groupBy { it.from }
        val visitedNodes = mutableSetOf<Node<*>>()

        val prettyPrint = StringBuilder()

        fun printNode(
            node: Node<*>,
            indent: Int,
        ) {
            if (node !in visitedNodes) {
                visitedNodes.add(node)
                prettyPrint.appendLine("  ".repeat(indent) + "${node.name}: ${node.data}")

                if (node in adjacencyList.keys) {
                    for (edge in adjacencyList[node]!!) {
                        prettyPrint.appendLine("  ".repeat(indent + 1) + "| ${edge.edgeName}")
                        printNode(edge.to, indent + 1)
                    }
                }
            }
        }

        val startNode =
            when (block != null) {
                true -> dag.nodes.first { block(it) }
                else -> dag.nodes.first()
            }
        printNode(startNode, 0)

        return prettyPrint.trim().toString()
    }
}
