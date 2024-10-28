package no.nav.dagpenger.opplysning

import no.nav.dagpenger.opplysning.regel.Ekstern
import no.nav.dagpenger.opplysning.regel.Regel
import java.time.LocalDate

// Regelverksdato: Datoen regelverket gjelder fra. Som hovedregel tidspunktet søknaden ble fremmet.

// Prøvingsdato: Dato som legges til grunn for når opplysninger som brukes av regelkjøringen skal være gyldige

// Virkningsdato: Dato som *behandlingen* finner til slutt

typealias Informasjonsbehov = Map<Opplysningstype<*>, List<Opplysning<*>>>

interface Forretningsprosess {
    fun regelsett(): List<Regelsett>

    fun ønsketResultat(opplysninger: LesbarOpplysninger): List<Opplysningstype<*>>
}

private class Regelsettprosess(
    val regelsett: List<Regelsett>,
) : Forretningsprosess {
    override fun regelsett() = regelsett

    override fun ønsketResultat(opplysninger: LesbarOpplysninger): List<Opplysningstype<*>> = regelsett.flatMap { it.produserer }
}

class Regelkjøring(
    private val regelverksdato: LocalDate,
    private val prøvingsdato: LocalDate,
    private val opplysninger: Opplysninger,
    private val forretningsprosess: Forretningsprosess,
) {
    constructor(regelverksdato: LocalDate, opplysninger: Opplysninger, vararg regelsett: Regelsett) : this(
        regelverksdato,
        regelverksdato,
        opplysninger,
        Regelsettprosess(regelsett.toList()),
    )

    constructor(regelverksdato: LocalDate, opplysninger: Opplysninger, forretningsprosess: Forretningsprosess) : this(
        regelverksdato,
        regelverksdato,
        opplysninger,
        forretningsprosess,
    )

    private val regelsett get() = forretningsprosess.regelsett()
    private val alleRegler: List<Regel<*>> get() = regelsett.flatMap { it.regler(regelverksdato) }

    private val ønsketResultat get() = forretningsprosess.ønsketResultat(opplysninger)

    // Finn bare regler som kreves for ønsket resultat
    // Kjører regler i topologisk rekkefølge
    private val gjeldendeRegler: List<Regel<*>> get() = alleRegler
    private val plan: MutableSet<Regel<*>> = mutableSetOf()
    private val kjørteRegler: MutableList<Regel<*>> = mutableListOf()

    private val opplysningerPåPrøvingsdato get() = opplysninger.forDato(prøvingsdato)

    init {
        val duplikate = gjeldendeRegler.groupBy { it.produserer }.filter { it.value.size > 1 }

        require(duplikate.isEmpty()) {
            "Regelsett inneholder flere regler som produserer samme opplysningstype. " +
                "Regler: ${duplikate.map { it.key.navn }}."
        }
    }

    fun evaluer(): Regelkjøringsrapport {
        aktiverRegler()
        while (plan.size > 0) {
            if (plan.all { it is Ekstern<*> }) {
                // Vi stopper opp for å kjøre behov når vi treffer regler som trenger eksterne opplysninger
                trenger.addAll(plan)
                break
            }
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
        ønsketResultat.forEach { opplysningstype ->
            val produsent = gjeldendeRegler.single { it.produserer(opplysningstype) }
            produsent.lagPlan(opplysningerPåPrøvingsdato, plan, gjeldendeRegler)
        }
    }

    private fun kjørRegelPlan() {
        while (plan.size > 0) {
            kjør(plan.first())
        }
    }

    private fun kjør(regel: Regel<*>) {
        if (regel is Ekstern<*>) {
            trenger.add(regel)
            plan.remove(regel)
            return
        }
        println("Lager ${regel.produserer.navn}")
        val opplysning = regel.lagProdukt(opplysningerPåPrøvingsdato)
        kjørteRegler.add(regel)
        plan.remove(regel)
        opplysninger.leggTilUtledet(opplysning)
    }

    private val trenger = mutableSetOf<Regel<*>>()

    private fun trenger(): Set<Opplysningstype<*>> {
        val eksterneOpplysninger = trenger.map { it.produserer }.toSet()
        return eksterneOpplysninger
    }

    private fun informasjonsbehov(): Informasjonsbehov =
        trenger()
            .associateWith {
                // Finn regel som produserer opplysningstype og hent ut avhengigheter
                gjeldendeRegler.find { regel -> regel.produserer(it) }?.avhengerAv ?: emptyList()
            }.filter { (_, avhengigheter) ->
                // Finn bare opplysninger hvor alle avhengigheter er tilfredsstilt
                avhengigheter.all { opplysningerPåPrøvingsdato.har(it) }
            }.mapValues { (_, avhengigheter) ->
                // Finn verdien av avhengighetene
                avhengigheter.map { opplysningerPåPrøvingsdato.finnOpplysning(it) }
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

class RegelGraph(
    regler: List<Regel<*>>,
) {
    private val graph = mutableMapOf<Opplysningstype<*>, MutableList<Opplysningstype<*>>>()

    init {
        regler.forEach { regel ->
            regel.avhengerAv.forEach { dependency ->
                graph.computeIfAbsent(dependency) { mutableListOf() }.add(regel.produserer)
            }
            graph.putIfAbsent(regel.produserer, mutableListOf()) // Ensure produserer is in the graph
        }
    }

    fun toMermaid(): String {
        val sb = StringBuilder("graph TD\n")
        graph.forEach { (from, toList) ->
            toList.forEach { to ->
                sb.append("    ${from.navn} --> ${to.navn}\n")
            }
        }
        return sb.toString()
    }
}
