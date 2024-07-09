package no.nav.dagpenger.dag.printer

import no.nav.dagpenger.dag.Node

typealias RootNodeFinner = (Node<*>) -> Boolean

interface DAGPrinter {
    fun toPrint(block: RootNodeFinner? = null): String
}
