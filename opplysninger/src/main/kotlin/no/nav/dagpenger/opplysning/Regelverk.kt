package no.nav.dagpenger.opplysning

import no.nav.dagpenger.dag.DAG
import no.nav.dagpenger.dag.Edge
import no.nav.dagpenger.dag.Node
import java.time.LocalDate

class Regelverk(
    vararg regelsett: Regelsett,
) {
    private val produsent: Map<Opplysningstype<*>, Regelsett> =
        regelsett.flatMap { rs -> rs.produserer.map { it to rs } }.toMap()

    fun reglerFor(
        opplysningstype: Opplysningstype<*>,
        forDato: LocalDate = LocalDate.MIN,
    ): List<Any> = regelsettFor(opplysningstype).flatMap { it.regler(forDato) }

    fun regelsettFor(opplysningstype: Opplysningstype<*>): List<Regelsett> {
        val nødvendigeRegelsett = mutableSetOf<Regelsett>()

        traverseOpplysningstyper(opplysningstype) { regelsett ->
            nødvendigeRegelsett.add(regelsett)
        }

        return nødvendigeRegelsett.toList()
    }

    fun regeltreFor(opplysningstype: Opplysningstype<*>): DAG<Regelsett, String> {
        val edges = mutableSetOf<Edge<Regelsett, String>>()

        traverseOpplysningstyper(opplysningstype) { currentRegelsett ->
            for (avhengighet in currentRegelsett.avhengerAv) {
                val til = produsent[avhengighet] ?: continue
                edges.add(Edge(Node(currentRegelsett.navn, currentRegelsett), Node(til.navn, til), "avhenger av"))
            }
        }

        return DAG(edges.toList())
    }

    // Bruker Breadth-First Search (BFS) til å traversere regelsettene
    private fun traverseOpplysningstyper(
        start: Opplysningstype<*>,
        block: (Regelsett) -> Unit,
    ) {
        val visited = mutableSetOf<Opplysningstype<*>>()
        val queue = ArrayDeque<Opplysningstype<*>>()
        queue.add(start)

        while (queue.isNotEmpty()) {
            val currentOpplysningstype = queue.removeFirst()
            val produseresAv = produsent[currentOpplysningstype] ?: continue

            if (visited.add(currentOpplysningstype)) {
                block(produseresAv)
                queue.addAll(produseresAv.avhengerAv)
            }
        }
    }
}
