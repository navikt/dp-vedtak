package no.nav.dagpenger.opplysning

import no.nav.dagpenger.opplysning.dag.RegeltreBygger
import no.nav.dagpenger.opplysning.regel.Ekstern
import no.nav.dagpenger.opplysning.regel.Regel
import java.time.LocalDate

class Regelkjøring(
    val forDato: LocalDate,
    private val opplysninger: Opplysninger,
    vararg regelsett: Regelsett,
) {
    private val alleRegler: List<Regel<*>> = regelsett.flatMap { it.regler(forDato) }
    private val muligeRegler: MutableList<Regel<*>> = alleRegler.toMutableList()
    private val plan: MutableList<Regel<*>> = mutableListOf()
    private val kjørteRegler: MutableList<Regel<*>> = mutableListOf()

    init {
        require(muligeRegler.groupBy { it.produserer }.all { it.value.size == 1 }) {
            "Regelsett inneholder flere regler som produserer samme opplysningstype."
        }
        opplysninger.registrer(this)
    }

    fun leggTil(opplysning: Opplysning<*>) {
        opplysninger.leggTil(opplysning)
        evaluer()
    }

    fun evaluer() {
        aktiverRegler()
        while (plan.size > 0) {
            kjørRegelPlan()
            aktiverRegler()
        }
    }

    private fun aktiverRegler() {
        muligeRegler
            .filter {
                it.kanKjøre(opplysninger)
            }.forEach {
                plan.add(it)
            }
        plan.forEach {
            muligeRegler.remove(it)
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
        opplysninger.leggTilUtledet(opplysning)
    }

    internal fun trenger(): Set<Opplysningstype<*>> {
        val graph = RegeltreBygger(muligeRegler).dag()
        val opplysningerUtenRegel = graph.findLeafNodes()
        val opplysningerMedEksternRegel = graph.findNodesWithEdge { it.data is Ekstern<*> }
        return (opplysningerUtenRegel + opplysningerMedEksternRegel)
            .map { it.data }
            .filterNot { opplysninger.har(it) }
            .toSet()
    }

    fun informasjonsbehov(): Map<Opplysningstype<*>, List<Opplysning<*>>> =
        trenger()
            .associateWith {
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
