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
    val opplysningstypes: List<Opplysningstype<*>> = regelsett.flatMap { it.produserer },
) : Forretningsprosess {
    override fun regelsett() = regelsett

    override fun ønsketResultat(opplysninger: LesbarOpplysninger): List<Opplysningstype<*>> = opplysningstypes
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

    private val ønsketResultat get() = forretningsprosess.ønsketResultat(opplysninger)

    // Finn bare regler som kreves for ønsket resultat
    // Kjører regler i topologisk rekkefølge
    private val gjeldendeRegler: List<Regel<*>> get() = alleRegler
    private var plan: MutableSet<Regel<*>> = mutableSetOf()
    private val kjørteRegler: MutableList<Regel<*>> = mutableListOf()
    private var trenger = setOf<Regel<*>>()

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
            kjørRegelPlan()
            aktiverRegler()
        }

        println("-------------------")
        val o = mutableSetOf<Opplysningstype<*>>()
        o.addAll(ønsketResultat)
        ønsketResultat.forEach { opplysningstype ->
            val opplysning = opplysningerPåPrøvingsdato.finnOpplysning(opplysningstype)
            opplysning.utledetAv?.opplysninger?.forEach { utledetOpplysning ->
                o.add(utledetOpplysning.opplysningstype)
            }
        }

        opplysningerPåPrøvingsdato.finnAlle().forEach { opplysning ->
            if (o.contains(opplysning.opplysningstype)) {
                println("vi beholder $opplysning")
            } else {
                println("Vi kaster $opplysning")
            }
        }

        return Regelkjøringsrapport(
            kjørteRegler = kjørteRegler,
            mangler = trenger(),
            informasjonsbehov = informasjonsbehov(),
            foreldreløse = opplysninger.foreldreløse(),
        )
    }

    private fun aktiverRegler() {
        val produksjonsplan = mutableSetOf<Regel<*>>()
        ønsketResultat.forEach { opplysningstype ->
            val produsent = gjeldendeRegler.single { it.produserer(opplysningstype) }
            produsent.lagPlan(opplysningerPåPrøvingsdato, produksjonsplan, gjeldendeRegler)
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
        val opplysning = regel.lagProdukt(opplysningerPåPrøvingsdato)
        kjørteRegler.add(regel)
        plan.remove(regel)
        opplysninger.leggTilUtledet(opplysning)
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
