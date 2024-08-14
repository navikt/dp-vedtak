package no.nav.dagpenger.regel.fastsetting

import no.nav.dagpenger.grunnbelop.forDato
import no.nav.dagpenger.grunnbelop.getGrunnbeløpForRegel
import no.nav.dagpenger.opplysning.Opplysningstype
import no.nav.dagpenger.opplysning.Regelsett
import no.nav.dagpenger.opplysning.regel.høyesteAv
import no.nav.dagpenger.opplysning.regel.innhentMed
import no.nav.dagpenger.opplysning.regel.inntekt.filtrerRelevanteInntekter
import no.nav.dagpenger.opplysning.regel.inntekt.oppjuster
import no.nav.dagpenger.opplysning.regel.inntekt.summerFørstePeriode
import no.nav.dagpenger.opplysning.regel.minstAv
import no.nav.dagpenger.opplysning.regel.multiplikasjon
import no.nav.dagpenger.opplysning.regel.oppslag
import no.nav.dagpenger.opplysning.verdier.Beløp
import no.nav.dagpenger.opplysning.verdier.Inntekt
import no.nav.dagpenger.regel.Minsteinntekt
import no.nav.dagpenger.regel.Søknadstidspunkt
import no.nav.dagpenger.regel.Verneplikt
import java.time.LocalDate

object Dagpengegrunnlag {
    val inntekt = Opplysningstype.somInntekt("Inntekt")
    val oppjustertinntekt = Opplysningstype.somInntekt("Oppjustert inntekt")
    val relevanteinntekter = Opplysningstype.somInntekt("Tellende inntekt")
    private val søknadstidspunkt = Søknadstidspunkt.søknadstidspunkt
    private val grunnbeløp = Opplysningstype.somBeløp("Grunnbeløp for grunnlag")
    val verneplikt = Verneplikt.vurderingAvVerneplikt
    val inntektId = Minsteinntekt.inntektId
    private val inntekt12mnd = Opplysningstype.somBeløp("Inntektsgrunnlag 12 mnd")
    private val inntekt36mnd = Opplysningstype.somBeløp("Inntektsgrunnlag 36 mnd")
    val avkortet = Opplysningstype.somBeløp("Avkortet grunnlag")
    val uavkortet = Opplysningstype.somBeløp("Uavkortet grunnlag")
    val grunnlag = Opplysningstype.somBeløp("Grunnlag")
    val harAvkortet = Opplysningstype.somBoolsk("Har avkortet grunnlag")
    val beregningsregel = Opplysningstype.somTekst("Beregningsregel")

    val regelsett =
        Regelsett("Dagpengegrunnlag") {
            // Grunnlag siste 36
            // 1. Oppjustere periode for periode
            // 2. Regn ut uavkortet med å summere 3 perioder
            // 3. Regn ut avkortet med å summere 3 perioder:
            //    a. Sum av uavkortet
            //    b. Periode for periode

            // regel(avkortet) { avkortet(inntekt36mnd, seksGangerG) }
            // regel(avkortet, LocalDate.of(2021, 12, 17)) { avkortetMånedlig(inntekt36mnd, seksGangerG) }
            regel(inntekt) { innhentMed(inntektId) }
            regel(grunnbeløp) { oppslag(søknadstidspunkt) { grunnbeløpFor(it) } }
            regel(oppjustertinntekt) { oppjuster(grunnbeløp, inntekt) }
            regel(relevanteinntekter) { filtrerRelevanteInntekter(oppjustertinntekt) }

            val siste12 = siste12mnd(relevanteinntekter, grunnbeløp)
            regel(grunnlag) { høyesteAv(siste12) }
        }
}

private fun grunnbeløpFor(it: LocalDate) =
    getGrunnbeløpForRegel(no.nav.dagpenger.grunnbelop.Regel.Grunnlag)
        .forDato(it)
        .verdi
        .let { Beløp(it) }

private fun Regelsett.siste12mnd(
    inntekt: Opplysningstype<Inntekt>,
    grunnbeløp: Opplysningstype<Beløp>,
): Opplysningstype<Beløp> {
    val inntektSiste12 = Opplysningstype.somBeløp("Inntekt siste 12 mnd")

    @Suppress("ktlint:standard:property-naming")
    val `6G` = Opplysningstype.somDesimaltall("6 ganger grunnbeløp")
    val maksGrunnlag = Opplysningstype.somBeløp("Maks grunnlag")

    @Suppress("ktlint:standard:property-naming")
    val `12MånederGrunnlag` = Opplysningstype.somBeløp("Grunnlag siste 12 mnd.")

    regel(`6G`) { oppslag(Søknadstidspunkt.søknadstidspunkt) { 6.0 } }
    regel(inntektSiste12) { summerFørstePeriode(inntekt) }
    regel(maksGrunnlag) { multiplikasjon(grunnbeløp, `6G`) }

    regel(`12MånederGrunnlag`) { minstAv(inntektSiste12, maksGrunnlag) }

    return `12MånederGrunnlag`
}

private fun Regelsett.siste36mnd(inntekt: Opplysningstype<Inntekt>): Opplysningstype<Beløp> =
    Opplysningstype.somBeløp("Inntektsgrunnlag 36 mnd")
