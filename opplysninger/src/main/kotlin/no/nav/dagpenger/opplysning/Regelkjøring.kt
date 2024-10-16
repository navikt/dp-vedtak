package no.nav.dagpenger.opplysning

import no.nav.dagpenger.opplysning.dag.RegeltreBygger
import no.nav.dagpenger.opplysning.regel.Ekstern
import no.nav.dagpenger.opplysning.regel.Regel
import java.time.LocalDate

// Regelverksdato: Datoen regelverket gjelder fra. Som hovedregel tidspunktet søknaden ble fremmet.

// Prøvingsdato: Dato som legges til grunn for når opplysninger som brukes av regelkjøringen skal være gyldige

// Virkningsdato: Dato som *behandlingen* finner til slutt

typealias Informasjonsbehov = Map<Opplysningstype<*>, List<Opplysning<*>>>

interface Forretningsprosess {
    fun regelsett(opplysninger: Opplysninger): List<Regelsett>
}

private class Regelsettprosess(
    val regelsett: List<Regelsett>,
) : Forretningsprosess {
    override fun regelsett(opplysninger: Opplysninger) = regelsett
}

class Regelkjøring(
    private val regelverksdato: LocalDate,
    private val opplysninger: Opplysninger,
    private val forretningsprosess: Forretningsprosess,
) {
    constructor(regelverksdato: LocalDate, opplysninger: Opplysninger, vararg regelsett: Regelsett) : this(
        regelverksdato,
        opplysninger,
        Regelsettprosess(regelsett.toList()),
    )

    private val regelsett get() = forretningsprosess.regelsett(opplysninger)
    private val alleRegler: List<Regel<*>> get() = regelsett.flatMap { it.regler(regelverksdato) }
    private val muligeRegler: MutableList<Regel<*>> get() = alleRegler.toMutableList()
    private val plan: MutableList<Regel<*>> = mutableListOf()
    private val kjørteRegler: MutableList<Regel<*>> = mutableListOf()

    init {
        val duplikate = muligeRegler.groupBy { it.produserer }.filter { it.value.size > 1 }

        require(duplikate.isEmpty()) {
            "Regelsett inneholder flere regler som produserer samme opplysningstype. " +
                "Regler: ${duplikate.map { it.key.navn }}."
        }
    }

    @Deprecated("Bruk leggTil rett på opplysninger", ReplaceWith("opplysninger.leggTil(opplysning)"))
    fun leggTil(opplysning: Opplysning<*>) {
        opplysninger.leggTil(opplysning)
        // evaluer()
    }

    fun evaluer(): Regelkjøringsrapport {
        aktiverRegler()
        while (plan.size > 0) {
            kjørRegelPlan()
            aktiverRegler()
        }

        return Regelkjøringsrapport(
            kjørteRegler = kjørteRegler,
            mangler = trenger(),
            informasjonsbehov = informasjonsbehov(),
        )
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

    private fun trenger(): Set<Opplysningstype<*>> {
        val graph = RegeltreBygger(muligeRegler).dag()
        val opplysningerUtenRegel = graph.findLeafNodes()
        val opplysningerMedEksternRegel = graph.findNodesWithEdge { it.data is Ekstern<*> }
        return (opplysningerUtenRegel + opplysningerMedEksternRegel)
            .map { it.data }
            .filterNot { opplysninger.har(it) }
            .toSet()
    }

    private fun informasjonsbehov(): Informasjonsbehov =
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

data class Regelkjøringsrapport(
    val kjørteRegler: List<Regel<*>>,
    val mangler: Set<Opplysningstype<*>>,
    val informasjonsbehov: Informasjonsbehov,
) {
    fun manglerOpplysninger(): Boolean = mangler.isNotEmpty()

    fun erFerdig(): Boolean = !manglerOpplysninger()
}
