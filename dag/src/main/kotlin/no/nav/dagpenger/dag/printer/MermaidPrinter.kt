package no.nav.dagpenger.dag.printer

import no.nav.dagpenger.dag.DAG

class MermaidPrinter(
    private val dag: DAG<*, Any?>,
    private val retning: String = "RL",
) : DAGPrinter {
    private val nodeIds = NodeIds()

    override fun toPrint(block: RootNodeFinner?): String {
        require(block == null) { "MermaidPrinter does not support root node" }

        val diagram = StringBuilder()
        diagram.appendLine("graph $retning")
        dag.edges.forEach { edge ->
            val fromId = nodeIds.id(edge.from)
            val toId = nodeIds.id(edge.to)

            val fromNodeName = "$fromId[\"${edge.from.name}\"]"
            val toNodeName = "$toId[\"${edge.to.name}\"]"

            diagram.appendLine("  $fromNodeName -->|\"${edge.edgeName}\"| $toNodeName")
        }

        return diagram.trim().toString()
    }
}

private class NodeIds(
    private val idGenerator: AlphabetIdGenerator = AlphabetIdGenerator(),
    private val ids: HashMap<Any, String> = hashMapOf(),
) {
    fun id(node: Any) = ids.computeIfAbsent(node) { nextId() }

    private fun nextId() = idGenerator.getNextId()
}

private class AlphabetIdGenerator(
    startingId: String = "A",
) {
    private var currentId = startingId

    fun getNextId(): String {
        val nextId = currentId
        currentId = incrementId(currentId)
        return nextId
    }

    private fun incrementId(id: String): String {
        if (id.isEmpty()) return "A"

        val lastIndex = id.length - 1
        val lastChar = id[lastIndex]
        return if (lastChar < 'Z') {
            id.substring(0, lastIndex) + (lastChar + 1)
        } else {
            incrementId(id.substring(0, lastIndex)) + 'A'
        }
    }
}
