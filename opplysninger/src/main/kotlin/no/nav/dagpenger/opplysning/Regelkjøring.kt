package no.nav.dagpenger.opplysning

import no.nav.dagpenger.opplysning.dag.RegeltreBygger
import no.nav.dagpenger.opplysning.regel.Ekstern
import no.nav.dagpenger.opplysning.regel.Regel
import java.time.LocalDate
import java.time.LocalDateTime

class Regelkjøring(
    val forDato: LocalDateTime,
    private val opplysninger: Opplysninger,
    vararg regelsett: Regelsett,
) {
    constructor(
        forDato: LocalDate,
        opplysninger: Opplysninger,
        vararg regelsett: Regelsett,
    ) : this(forDato.atStartOfDay(), opplysninger, *regelsett)

    private val muligeRegler: MutableList<Regel<*>> = regelsett.flatMap { it.regler(forDato.toLocalDate()) }.toMutableList()
    private val plan: MutableList<Regel<*>> = mutableListOf()
    private val kjørteRegler: MutableList<Regel<*>> = mutableListOf()

    init {
        require(muligeRegler.groupBy { it.produserer }.all { it.value.size == 1 }) {
            "Regelsett inneholder flere regler som produserer samme opplysningstype."
        }
        opplysninger.registrer(this)
    }

    internal fun evaluer() {
        aktiverRegler()
        while (plan.size > 0) {
            kjørRegelPlan()
            aktiverRegler()
        }
    }

    private fun kjørRegelPlan() {
        while (plan.size > 0) {
            kjør(plan.first())
        }
    }

    private fun kjør(regel: Regel<*>) {
        val opplysning = regel.lagProdukt(opplysninger)
        kjørteRegler.add(regel)
        plan.remove(regel)
        opplysninger.leggTil(opplysning)
    }

    private fun aktiverRegler() {
        muligeRegler.filter {
            it.kanKjøre(opplysninger)
        }.forEach {
            plan.add(it)
        }
        plan.forEach {
            muligeRegler.remove(it)
        }
    }

    fun trenger(opplysningstype: Opplysningstype<*>? = null): Set<Opplysningstype<*>> {
        if (opplysningstype?.let { opplysninger.har(it) } == true) return emptySet()
        val dag = RegeltreBygger(muligeRegler).dag()
        val graph =
            when (opplysningstype) {
                null -> dag
                else -> dag.subgraph { it.er(opplysningstype) }
            }
        val opplysningerUtenRegel = graph.findLeafNodes()
        val opplysningerMedEksternRegel = graph.findNodesWithEdgeNamed(Ekstern::class.java.simpleName)
        return (opplysningerUtenRegel + opplysningerMedEksternRegel)
            .map { it.data }.filterNot { opplysninger.har(it) }.toSet()
    }

    fun informasjonsbehov(opplysningstype: Opplysningstype<*>): Map<Opplysningstype<*>, List<Opplysning<*>>> {
        return trenger(opplysningstype).associateWith {
            // Finn regel som produserer opplysningstype og hent ut avhengigheter
            muligeRegler.find { regel -> regel.produserer(it) }?.avhengerAv ?: emptyList()
        }.filter { (_, avhengigheter) ->
            // Finn bare opplysninger hvor alle avhengigheter er tilfredsstilt
            avhengigheter.all { opplysninger.har(it) }
        }.mapValues { (_, avhengigheter) ->
            // Finn verdien av avhengighetene
            avhengigheter.map { opplysninger.finnOpplysning(it) }
        }
    }
}
