package no.nav.dagpenger.opplysning

import mu.KotlinLogging
import no.nav.dagpenger.opplysning.regel.Ekstern
import no.nav.dagpenger.opplysning.regel.Regel
import java.time.LocalDate

// Regelverksdato: Datoen regelverket gjelder fra. Som hovedregel tidspunktet søknaden ble fremmet.

// Prøvingsdato: Dato som legges til grunn for når opplysninger som brukes av regelkjøringen skal være gyldige

// Virkningsdato: Dato som *behandlingen* finner til slutt

typealias Informasjonsbehov = Map<Opplysningstype<*>, List<Opplysning<*>>>

interface Forretningsprosess {
    val regelverk: Regelverk

    fun regelsett() = regelverk.regelsett

    fun ønsketResultat(opplysninger: LesbarOpplysninger): List<Opplysningstype<*>>
}

private class Regelsettprosess(
    val regelsett: List<Regelsett>,
    val opplysningstypes: List<Opplysningstype<*>> = regelsett.flatMap { it.produserer },
) : Forretningsprosess {
    override val regelverk: Regelverk
        get() = TODO("Not yet implemented")

    override fun regelsett() = regelsett

    override fun ønsketResultat(opplysninger: LesbarOpplysninger): List<Opplysningstype<*>> = opplysningstypes
}

class Regelkjøring(
    private val regelverksdato: LocalDate,
    private val prøvingsdato: LocalDate,
    private val opplysninger: Opplysninger,
    private val forretningsprosess: Forretningsprosess,
) {
    companion object {
        private val logger = KotlinLogging.logger { }
    }

    constructor(regelverksdato: LocalDate, opplysninger: Opplysninger, vararg regelsett: Regelsett) : this(
        regelverksdato,
        regelverksdato,
        opplysninger,
        Regelsettprosess(regelsett.toList(), regelsett.toList().flatMap { it.produserer }),
    )

    constructor(regelverksdato: LocalDate, opplysninger: Opplysninger, forretningsprosess: Forretningsprosess) : this(
        regelverksdato,
        regelverksdato,
        opplysninger,
        forretningsprosess,
    )

    constructor(
        regelverksdato: LocalDate,
        opplysninger: Opplysninger,
        ønskerResultat: List<Opplysningstype<*>>,
        vararg regelsett: Regelsett,
    ) : this(
        regelverksdato,
        regelverksdato,
        opplysninger,
        Regelsettprosess(regelsett.toList(), ønskerResultat),
    )

    private val regelsett get() = forretningsprosess.regelsett()
    private val alleRegler: List<Regel<*>> get() = regelsett.flatMap { it.regler(regelverksdato) }

    private val opplysningerPåPrøvingsdato get() = opplysninger.forDato(prøvingsdato)

    private val ønsketResultat get() = forretningsprosess.ønsketResultat(opplysningerPåPrøvingsdato)

    // Finn bare regler som kreves for ønsket resultat
    // Kjører regler i topologisk rekkefølge
    private val gjeldendeRegler: List<Regel<*>> get() = alleRegler
    private var plan: MutableSet<Regel<*>> = mutableSetOf()
    private val kjørteRegler: MutableList<Regel<*>> = mutableListOf()

    private var trenger = setOf<Regel<*>>()

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
            kjørRegelPlan()
            aktiverRegler()
        }

        val brukteOpplysninger = muligeOpplysninger()
        opplysninger.fjern(brukteOpplysninger)

        return Regelkjøringsrapport(
            kjørteRegler = kjørteRegler,
            mangler = trenger(),
            informasjonsbehov = informasjonsbehov(),
            foreldreløse = opplysninger.fjernet(),
        )
    }

    private fun muligeOpplysninger(): Set<Opplysningstype<*>> {
        val brukteOpplysninger = mutableSetOf<Opplysningstype<*>>()
        brukteOpplysninger.addAll(ønsketResultat)

        val regelMap = alleRegler.associateBy { it.produserer }

        val opplysningerUtenRegel =
            opplysningerPåPrøvingsdato
                .finnAlle()
                .filter { opplysning -> opplysning.opplysningstype !in regelMap }
                .map { it.opplysningstype }

        brukteOpplysninger.addAll(opplysningerUtenRegel)

        val muligeRegler = alleRegler.filterNot { opplysningerUtenRegel.contains(it.produserer) }

        ønsketResultat.forEach { opplysningstype ->
            val regel = regelMap[opplysningstype] ?: throw IllegalStateException("Fant ikke regel for $opplysningstype")
            brukteOpplysninger.add(regel.produserer)
            regel.avhengerAv.forEach { avhengighet ->
                val avhengigRegel = regelMap[avhengighet] ?: throw IllegalStateException("Fant ikke regel for $avhengighet")
                brukteOpplysninger.add(avhengigRegel.produserer)
                leggTilAvhengigRegel(avhengigRegel, brukteOpplysninger, muligeRegler, regelMap)
            }
        }
        return brukteOpplysninger.toSet()
    }

    private fun leggTilAvhengigRegel(
        avhengigRegel: Regel<*>,
        brukteOpplysninger: MutableSet<Opplysningstype<*>>,
        muligeRegler: List<Regel<*>>,
        regelMap: Map<Opplysningstype<out Comparable<*>>, Regel<*>>,
    ) {
        avhengigRegel.avhengerAv.forEach { avhengighet ->
            val regel = regelMap[avhengighet] ?: throw IllegalStateException("Fant ikke regel for $avhengighet")
            brukteOpplysninger.add(regel.produserer)
            leggTilAvhengigRegel(regel, brukteOpplysninger, muligeRegler, regelMap)
        }
    }

    private fun aktiverRegler() {
        val produksjonsplan = mutableSetOf<Regel<*>>()
        val produsenter = gjeldendeRegler.associateBy { it.produserer }
        ønsketResultat.forEach { opplysningstype ->
            val produsent =
                produsenter[opplysningstype]
                    ?: throw IllegalArgumentException("Fant ikke regel som produserer $opplysningstype")
            produsent.lagPlan(opplysningerPåPrøvingsdato, produksjonsplan, produsenter)
        }
        val (ekstern, intern) = produksjonsplan.partition { it is Ekstern<*> }
        plan = intern.toMutableSet()
        trenger = ekstern.toSet()
    }

    private fun kjørRegelPlan() {
        while (plan.size > 0) {
            kjør(plan.first())
        }
    }

    private fun kjør(regel: Regel<*>) {
        try {
            val opplysning = regel.lagProdukt(opplysningerPåPrøvingsdato)
            kjørteRegler.add(regel)
            plan.remove(regel)
            opplysninger.leggTilUtledet(opplysning)
        } catch (e: IllegalArgumentException) {
            logger.info {
                """
                Skal kjøre: 
                ${plan.joinToString("\n") { it.produserer.navn }}
                Har kjørt: 
                ${kjørteRegler.joinToString("\n") { it.produserer.navn }}
                """.trimIndent()
            }
            throw e
        }
    }

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
    val foreldreløse: List<Opplysning<*>>,
) {
    fun manglerOpplysninger(): Boolean = mangler.isNotEmpty()

    fun erFerdig(): Boolean = !manglerOpplysninger()
}
