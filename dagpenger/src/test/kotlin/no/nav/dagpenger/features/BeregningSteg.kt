package no.nav.dagpenger.features

import io.cucumber.datatable.DataTable
import io.cucumber.java8.No
import io.kotest.matchers.shouldBe
import no.nav.dagpenger.opplysning.Faktum
import no.nav.dagpenger.opplysning.Gyldighetsperiode
import no.nav.dagpenger.opplysning.Opplysning
import no.nav.dagpenger.opplysning.Opplysninger
import no.nav.dagpenger.opplysning.verdier.Beløp
import no.nav.dagpenger.regel.TapAvArbeidsinntektOgArbeidstid.fastsattVanligArbeidstid
import no.nav.dagpenger.regel.beregning.Beregning.arbeidsdag
import no.nav.dagpenger.regel.beregning.Beregning.arbeidstimer
import no.nav.dagpenger.regel.beregning.Beregning.forbruk
import no.nav.dagpenger.regel.beregning.Beregning.terskel
import no.nav.dagpenger.regel.beregning.BeregningsperiodeFabrikk
import no.nav.dagpenger.regel.fastsetting.DagpengenesStørrelse.sats
import no.nav.dagpenger.regel.fastsetting.Dagpengeperiode.antallStønadsuker
import no.nav.dagpenger.regel.fastsetting.Dagpengeperiode.gjenståendeStønadsdager
import no.nav.dagpenger.regel.fastsetting.Egenandel.egenandel
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

class BeregningSteg : No {
    private val opplysninger = mutableListOf<Opplysning<*>>()
    private lateinit var meldeperiodeFraOgMed: LocalDate
    private lateinit var meldeperiodeTilOgMed: LocalDate

    init {
        Gitt("at terskel er satt til {double}") { terskelVerdi: Double ->
            opplysninger.add(Faktum(terskel, terskelVerdi))
        }
        Gitt("at mottaker har vedtak med") { dataTable: DataTable? ->
            val vedtakstabell = dataTable!!.asMaps()
            opplysninger.addAll(lagVedtak(vedtakstabell))
        }
        Når("meldekort for periode som begynner fra og med {dato} mottas med") { fraOgMed: LocalDate, dataTable: DataTable? ->
            val dager = dataTable!!.asMaps()
            meldeperiodeFraOgMed = fraOgMed
            meldeperiodeTilOgMed = meldeperiodeFraOgMed.plusDays(13)
            opplysninger.addAll(lagMeldekort(fraOgMed, dager))
        }
        Så("skal kravet til tapt arbeidstid ikke være oppfylt") {
            beregning.oppfyllerKravTilTaptArbeidstid shouldBe false
        }
        Så("skal kravet til tapt arbeidstid være oppfylt") {
            beregning.oppfyllerKravTilTaptArbeidstid shouldBe true
        }
        Så("utbetales {double} kroner") { utbetaling: Double ->
            beregning.oppfyllerKravTilTaptArbeidstid shouldBe true
            beregning.utbetaling shouldBe utbetaling
        }
        Så("det forbrukes {int} dager") { dager: Int ->
            beregning.forbruksdager.size shouldBe dager

            beregning.forbruksdager.forEach {
                opplysninger.add(Faktum(forbruk, true, Gyldighetsperiode(it.dato, it.dato)))
            }
        }
        Så("utbetales {double} kroner på dag {int}") { utbetaling: Double, dag: Int ->
            beregning.forbruksdager[dag - 1].tilUtbetaling shouldBe utbetaling
        }
        Så("det gjenstår {int} dager") { dager: Int ->
            // TODO: Dette må bo et sted
            val utgangspunkt = opplysninger.find { it.opplysningstype == gjenståendeStønadsdager }!!.verdi as Int
            val forbrukteDager = opplysninger.filter { it.opplysningstype == forbruk }.size
            val gjenståendeDager = utgangspunkt - forbrukteDager
            gjenståendeDager shouldBe dager

            opplysninger.add(Faktum(gjenståendeStønadsdager, gjenståendeDager, Gyldighetsperiode(fom = meldeperiodeTilOgMed)))
        }
        Så("det gjenstår {int} ventedager") { dager: Int ->
            // TODO: Dette må bo et sted
            val utgangspunkt = opplysninger.find { it.opplysningstype == gjenståendeStønadsdager }!!.verdi as Int
            val forbrukteDager = opplysninger.filter { it.opplysningstype == forbruk }.size
            val gjenståendeDager = utgangspunkt - forbrukteDager
            gjenståendeDager shouldBe dager

            opplysninger.add(Faktum(gjenståendeStønadsdager, gjenståendeDager, Gyldighetsperiode(fom = meldeperiodeTilOgMed)))
        }
        Og("det forbrukes {int} i egenandel") { forbruktEgenandel: Int ->
            beregning.forbruksdager.sumOf { it.forbruktEgenandel } shouldBe forbruktEgenandel.toDouble()
        }
        Og("gjenstår {int} i egenandel") { gjenståendeEgenandel: Int ->
            val egenandel = opplysninger.find { it.opplysningstype == egenandel }!!.verdi as Beløp
            val forbrukt = beregning.forbruksdager.sumOf { it.forbruktEgenandel }

            egenandel.verdien.toInt() - forbrukt shouldBe gjenståendeEgenandel
        }
    }

    private val beregning by lazy {
        val opplysninger = Opplysninger(opplysninger)
        BeregningsperiodeFabrikk(meldeperiodeFraOgMed, meldeperiodeTilOgMed, opplysninger).lagBeregningsperiode()
    }

    private fun lagVedtak(vedtakstabell: List<MutableMap<String, String>>): List<Opplysning<*>> =
        vedtakstabell.map {
            val factory = opplysningFactory(it)
            factory(it, gyldighetsperiode(it["fraOgMed"].toLocalDate(), it["tilOgMed"].toLocalDate()))
        }

    private companion object {
        val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy", Locale.getDefault())
    }

    private fun String?.toLocalDate() = this?.let { LocalDate.parse(it, formatter) }

    private fun gyldighetsperiode(
        gyldigFraOgMed: LocalDate? = null,
        gyldigTilOgMed: LocalDate? = null,
    ): Gyldighetsperiode =

        if (gyldigFraOgMed != null && gyldigTilOgMed != null) {
            Gyldighetsperiode(gyldigFraOgMed, gyldigTilOgMed)
        } else if (gyldigFraOgMed != null && gyldigTilOgMed == null) {
            Gyldighetsperiode(gyldigFraOgMed)
        } else if (gyldigTilOgMed != null) {
            Gyldighetsperiode(tom = gyldigTilOgMed)
        } else {
            Gyldighetsperiode()
        }

    private fun lagMeldekort(
        fraOgMed: LocalDate,
        dager: MutableList<MutableMap<String, String>>,
    ): List<Opplysning<*>> {
        require(dager.size == 14) { "Må ha nøyaktig 14 dager" }
        require(fraOgMed.dayOfWeek.value == 1) { "Må starte på en mandag" }
        val opplysninger =
            dager
                .mapIndexed { i, dag ->
                    val dato = fraOgMed.plusDays(i.toLong())
                    val timer = dag["verdi"]?.toInt() ?: 0
                    val type = dag["type"] ?: "Arbeidstimer"
                    when (type) {
                        "Arbeidstimer" ->
                            listOf(
                                Faktum(arbeidstimer, timer, Gyldighetsperiode(dato, dato)),
                                Faktum(arbeidsdag, true, Gyldighetsperiode(dato, dato)),
                            )

                        "Fravær",
                        "Sykdom",
                        -> listOf(Faktum(arbeidsdag, false, Gyldighetsperiode(dato, dato)))

                        else -> throw IllegalArgumentException("Ukjent dagtype: $type")
                    }
                }.flatten()
        return opplysninger
    }

    private val opplysningFactories: Map<String, (Map<String, String>, Gyldighetsperiode) -> Opplysning<*>> =
        mapOf(
            "Innvilget periode" to { args, gyldighetsperiode ->
                Faktum(antallStønadsuker, args["verdi"]!!.toInt(), gyldighetsperiode)
            },
            "Gjenstående stønadsdager" to { args, gyldighetsperiode ->
                Faktum(gjenståendeStønadsdager, args["verdi"]!!.toInt(), gyldighetsperiode)
            },
            "Sats" to { args, gyldighetsperiode ->
                Faktum(sats, Beløp(args["verdi"]!!.toInt()), gyldighetsperiode)
            },
            "FVA" to { args, gyldighetsperiode ->
                Faktum(fastsattVanligArbeidstid, args["verdi"]!!.toDouble(), gyldighetsperiode)
            },
            "Terskel" to { args, gyldighetsperiode ->
                Faktum(terskel, args["verdi"]!!.toDouble(), gyldighetsperiode)
            },
            "Egenandel" to { args, gyldighetsperiode ->
                Faktum(egenandel, Beløp(args["verdi"]!!.toInt()), gyldighetsperiode)
            },
        )

    private fun opplysningFactory(it: Map<String, String>): (Map<String, String>, Gyldighetsperiode) -> Opplysning<*> {
        val opplysningstype = it["Opplysning"]!!
        return opplysningFactories[opplysningstype] ?: throw IllegalArgumentException("Ukjent opplysningstype: $opplysningstype")
    }
}
