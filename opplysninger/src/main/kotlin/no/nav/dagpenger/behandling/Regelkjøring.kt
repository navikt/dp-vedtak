package no.nav.dagpenger.behandling

import no.nav.dagpenger.behandling.dag.RegeltreBygger
import no.nav.dagpenger.behandling.regel.Regel
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

    private val muligeRegler: MutableList<Regel<*>> =
        regelsett.flatMap { it.regler(forDato.toLocalDate()) }.toMutableList()
    private val plan: MutableList<Regel<*>> = mutableListOf()
    private val kjørteRegler: MutableList<Regel<*>> = mutableListOf()

    init {
        require(muligeRegler.groupBy { it.produserer }.all { it.value.size == 1 }) {
            "Regelsett inneholder flere regler som produserer samme opplysningstype."
        }
        opplysninger.registrer(this)
    }

    fun evaluer() {
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

    fun trenger(opplysningstype: Opplysningstype<*>): Set<Opplysningstype<*>> {
        if (opplysninger.har(opplysningstype)) return emptySet()
        val dag = RegeltreBygger(muligeRegler).dag()
        return dag
            .subgraph { it.er(opplysningstype) }
            .findLeafNodes()
            .map { it.data }
            .filterNot { opplysninger.har(it) }.toSet()
    }
}
