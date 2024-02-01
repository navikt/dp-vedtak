package no.nav.dagpenger.behandling.dag

import no.nav.dagpenger.behandling.Opplysning
import no.nav.dagpenger.behandling.Opplysninger

class DatatreBygger(private val opplysninger: Opplysninger) {
    private val nodes = mutableListOf<Node<Opplysning<*>>>()
    private val edges = mutableListOf<Edge<Opplysning<*>>>()

    fun dag(): DAG<Opplysning<*>> {
        opplysninger.finnAlle().forEach { opplysning ->
            val element = Node("${opplysning.opplysningstype.navn}: ${opplysning.verdi}", opplysning)
            nodes.add(element)
            opplysning.utledetAv?.opplysninger?.forEach { utledning ->
                val utledningNode = Node("${utledning.opplysningstype.navn}: ${utledning.verdi}", utledning)
                nodes.add(utledningNode)
                edges.add(Edge(from = utledningNode, to = element, edgeName = "Brukes av"))
            }
        }
        return DAG(nodes = nodes, edges = edges)
    }
}
