package no.nav.dagpenger.opplysning.dag

import no.nav.dagpenger.opplysning.Opplysning
import no.nav.dagpenger.opplysning.Opplysninger

class DatatreBygger(private val opplysninger: Opplysninger) {
    private val edges = mutableListOf<Edge<Opplysning<*>>>()

    fun dag(): DAG<Opplysning<*>> {
        opplysninger.finnAlle().forEach { opplysning ->
            val element = Node("${opplysning.opplysningstype.navn}: ${opplysning.verdi}", opplysning)
            opplysning.utledetAv?.opplysninger?.forEach { utledning ->
                val utledningNode = Node("${utledning.opplysningstype.navn}: ${utledning.verdi}", utledning)
                edges.add(Edge(from = utledningNode, to = element, edgeName = "brukes til"))
            }
        }
        return DAG(edges = edges)
    }
}
