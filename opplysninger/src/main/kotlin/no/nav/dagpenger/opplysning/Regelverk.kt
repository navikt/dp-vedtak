package no.nav.dagpenger.opplysning

import no.nav.dagpenger.dag.DAG
import no.nav.dagpenger.dag.Edge
import no.nav.dagpenger.dag.Node

class Regelverk(
    vararg regelsett: Regelsett,
) {
    private val regelsett = regelsett.toList()

    fun regelsettFor(opplysningstype: Opplysningstype<*>): List<Regelsett> {
        val result = mutableSetOf<Regelsett>()
        val queue = ArrayDeque<Opplysningstype<*>>()
        queue.add(opplysningstype)

        while (queue.isNotEmpty()) {
            val currentOpplysningstype = queue.removeFirst()
            val relevantRegelsett = regelsett.filter { it.produserer(currentOpplysningstype) }

            for (rs in relevantRegelsett) {
                if (result.add(rs)) {
                    queue.addAll(rs.avhengerAv)
                }
            }
        }

        return result.toList()
    }

    fun regeltreFor(opplysningstype: Opplysningstype<*>): DAG<Regelsett, String> {
        val kanter = mutableSetOf<Edge<Regelsett, String>>()
        val queue = ArrayDeque<Opplysningstype<*>>()
        queue.add(opplysningstype)

        while (queue.isNotEmpty()) {
            val currentOpplysningstype = queue.removeFirst()
            val fra = regelsett.single { it.produserer(currentOpplysningstype) }

            val avhengigheter = fra.avhengerAv
            for (avhengighet in avhengigheter) {
                val til = regelsett.single { it.produserer(avhengighet) }
                if (kanter.add(Edge(Node(fra.navn, fra), Node(til.navn, til), "avhenger av"))) {
                    queue.add(avhengighet)
                }
            }
        }

        return DAG(kanter.toList())
    }

    fun reglerFor(opplysningstype: Opplysningstype<*>) = regelsettFor(opplysningstype).flatMap { it.regler() }
}
