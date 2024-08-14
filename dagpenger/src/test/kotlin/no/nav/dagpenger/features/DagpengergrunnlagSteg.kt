package no.nav.dagpenger.features

import io.cucumber.datatable.DataTable
import io.cucumber.java.BeforeStep
import io.cucumber.java8.No
import io.kotest.matchers.shouldBe
import no.nav.dagpenger.dato.mai
import no.nav.dagpenger.features.DagpengergrunnlagSteg.Månedsinntekt.Companion.finnSisteAvsluttendeKalenderMåned
import no.nav.dagpenger.inntekt.v1.InntektKlasse
import no.nav.dagpenger.inntekt.v1.KlassifisertInntekt
import no.nav.dagpenger.inntekt.v1.KlassifisertInntektMåned
import no.nav.dagpenger.opplysning.Faktum
import no.nav.dagpenger.opplysning.Opplysninger
import no.nav.dagpenger.opplysning.Regelkjøring
import no.nav.dagpenger.opplysning.verdier.Inntekt
import no.nav.dagpenger.regel.RegelverkDagpenger
import no.nav.dagpenger.regel.Verneplikt.vurderingAvVerneplikt
import no.nav.dagpenger.regel.fastsetting.Dagpengegrunnlag
import no.nav.dagpenger.regel.fastsetting.Dagpengegrunnlag.avkortet
import java.math.BigDecimal
import java.time.YearMonth
import java.util.UUID
import no.nav.dagpenger.inntekt.v1.Inntekt as InntektV1

class DagpengergrunnlagSteg : No {
    private val fraDato = 10.mai(2022)
    private val regelsett = RegelverkDagpenger.regelsettFor(avkortet)
    private val opplysninger: Opplysninger = Opplysninger()
    private lateinit var regelkjøring: Regelkjøring

    @BeforeStep
    fun kjørRegler() {
        regelkjøring = Regelkjøring(fraDato, opplysninger, *regelsett.toTypedArray())
    }

    init {
        Gitt("at verneplikt for grunnlag er satt {boolsk}") { verneplikt: Boolean ->
            regelkjøring.leggTil(Faktum(vurderingAvVerneplikt, verneplikt))
        }

        Gitt("at inntekt for grunnlag er") { dataTable: DataTable? ->
            val inntektstabell = dataTable!!.asMaps()
            val f = Inntekt(lagInntekt(inntektstabell))
            regelkjøring.leggTil(Faktum(Dagpengegrunnlag.inntekt, f))
        }

        Så("beregnet grunnlag være {string} og {string}") { avkortet: String, uavkortet: String ->

//            opplysninger.finnOpplysning(Dagpengegrunnlag.avkortet).verdi shouldBe Beløp(avkortet.toBigDecimal())
//            opplysninger.finnOpplysning(Dagpengegrunnlag.uavkortet).verdi shouldBe Beløp(uavkortet.toBigDecimal())
        }

        Og("vi har ikke avkortet") {
            opplysninger.finnOpplysning(Dagpengegrunnlag.harAvkortet).verdi shouldBe false
        }
    }

    private fun lagInntekt(inntekt: MutableList<MutableMap<String, String>>): InntektV1 {
        val inntekter = inntekt.map { Månedsinntekt(it) }
        val perMåned = inntekter.groupBy { it.periode }
        return InntektV1(
            inntektsId = UUID.randomUUID().toString(),
            inntektsListe =
                perMåned.map { (periode, månedsinntekter) ->
                    KlassifisertInntektMåned(
                        årMåned = periode,
                        klassifiserteInntekter =
                            månedsinntekter.map {
                                KlassifisertInntekt(
                                    beløp = it.beløp,
                                    inntektKlasse = it.inntektsklasse,
                                )
                            },
                    )
                },
            sisteAvsluttendeKalenderMåned = inntekter.finnSisteAvsluttendeKalenderMåned(),
        )
    }

    private data class Månedsinntekt(
        val periode: YearMonth,
        val beløp: BigDecimal,
        val inntektsklasse: InntektKlasse,
    ) {
        constructor(verdier: Map<String, String>) : this(
            verdier["Periode"]!!.let { YearMonth.parse(it) },
            verdier["Beløp"]!!.toBigDecimal(),
            verdier["Inntektsklasse"]!!.let { InntektKlasse.valueOf(it) },
        )

        companion object {
            fun List<Månedsinntekt>.finnSisteAvsluttendeKalenderMåned() = this.maxByOrNull { it.periode }!!.periode
        }
    }
}
