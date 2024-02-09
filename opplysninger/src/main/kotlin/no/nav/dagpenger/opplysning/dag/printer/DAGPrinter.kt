package no.nav.dagpenger.opplysning.dag.printer

import no.nav.dagpenger.opplysning.dag.Node

typealias RootNodeFinner = (Node<*>) -> Boolean

interface DAGPrinter {
    fun toPrint(block: RootNodeFinner? = null): String
}
