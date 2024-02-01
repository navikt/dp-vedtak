package no.nav.dagpenger.behandling.dag.printer

import no.nav.dagpenger.behandling.dag.Node

typealias RootNodeFinner = (Node<*>) -> Boolean

interface DAGPrinter {
    fun toPrint(block: RootNodeFinner? = null): String
}
