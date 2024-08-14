package no.nav.dagpenger.regel.fastsetting

import no.nav.dagpenger.grunnbelop.forDato
import no.nav.dagpenger.grunnbelop.getGrunnbeløpForRegel
import no.nav.dagpenger.inntekt.v1.InntektKlasse
import no.nav.dagpenger.opplysning.Opplysningstype
import no.nav.dagpenger.opplysning.Regelsett
import no.nav.dagpenger.opplysning.regel.divisjon
import no.nav.dagpenger.opplysning.regel.høyesteAv
import no.nav.dagpenger.opplysning.regel.innhentMed
import no.nav.dagpenger.opplysning.regel.inntekt.SummerPeriode
import no.nav.dagpenger.opplysning.regel.inntekt.filtrerRelevanteInntekter
import no.nav.dagpenger.opplysning.regel.inntekt.oppjuster
import no.nav.dagpenger.opplysning.regel.inntekt.sumAv
import no.nav.dagpenger.opplysning.regel.inntekt.summerPeriode
import no.nav.dagpenger.opplysning.regel.minstAv
import no.nav.dagpenger.opplysning.regel.multiplikasjon
import no.nav.dagpenger.opplysning.regel.oppslag
import no.nav.dagpenger.opplysning.verdier.Beløp
import no.nav.dagpenger.opplysning.verdier.Inntekt
import no.nav.dagpenger.regel.Minsteinntekt
import no.nav.dagpenger.regel.Søknadstidspunkt
import no.nav.dagpenger.regel.fastsetting.Dagpengegrunnlag.maksgrenseForGrunnlag
import java.time.LocalDate

@Suppress("ktlint:standard:property-naming")
object Dagpengegrunnlag {
    val inntekt = Opplysningstype.somInntekt("Inntekt")
    val oppjustertinntekt = Opplysningstype.somInntekt("Oppjustert inntekt")
    val relevanteinntekter = Opplysningstype.somInntekt("Tellende inntekt")
    val søknadstidspunkt = Søknadstidspunkt.søknadstidspunkt
    private val grunnbeløp = Opplysningstype.somBeløp("Grunnbeløp for grunnlag")
    private val inntektId = Minsteinntekt.inntektId
    val grunnlag = Opplysningstype.somBeløp("Grunnlag")
    val harAvkortet = Opplysningstype.somBoolsk("Har avkortet grunnlag")

    val faktorForMaksgrense = Opplysningstype.somDesimaltall("Faktor")
    val maksgrenseForGrunnlag = Opplysningstype.somBeløp("6 ganger grunnbeløp")

    val regelsett =
        Regelsett("Dagpengegrunnlag") {
            regel(faktorForMaksgrense) { oppslag(Søknadstidspunkt.søknadstidspunkt) { 6.0 } }
            regel(maksgrenseForGrunnlag) { multiplikasjon(grunnbeløp, faktorForMaksgrense) }

            regel(inntekt) { innhentMed(inntektId) }
            regel(grunnbeløp) { oppslag(søknadstidspunkt) { grunnbeløpFor(it) } }
            regel(oppjustertinntekt) { oppjuster(grunnbeløp, inntekt) }
            regel(relevanteinntekter) {
                filtrerRelevanteInntekter(
                    oppjustertinntekt,
                    listOf(
                        InntektKlasse.ARBEIDSINNTEKT,
                        InntektKlasse.DAGPENGER,
                        InntektKlasse.SYKEPENGER,
                        InntektKlasse.TILTAKSLØNN,
                        InntektKlasse.PLEIEPENGER,
                        InntektKlasse.OPPLÆRINGSPENGER,
                        InntektKlasse.OMSORGSPENGER,
                    ),
                )
            }

            val siste12 = siste12mnd(relevanteinntekter)
            val siste36 = siste36mnd(relevanteinntekter)
            regel(grunnlag) { høyesteAv(siste36, siste12) }
        }
}

private fun Regelsett.siste12mnd(inntekt: Opplysningstype<Inntekt>): Opplysningstype<Beløp> {
    val inntektSiste12 = Opplysningstype.somBeløp("Inntekt siste 12 mnd")
    val grunnlag = Opplysningstype.somBeløp("Grunnlag siste 12 mnd.")

    regel(inntektSiste12) { summerPeriode(inntekt, SummerPeriode.InntektPeriode.Første) }
    regel(grunnlag) { minstAv(inntektSiste12, maksgrenseForGrunnlag) }

    return grunnlag
}

private fun Regelsett.siste36mnd(inntekt: Opplysningstype<Inntekt>): Opplysningstype<Beløp> {
    val beløpSiste36 = Opplysningstype.somBeløp("Inntekt siste 36 måneder")
    val årligGjennomsnittSiste36 = Opplysningstype.somBeløp("Gjennomsnittlig arbeidsinntekt siste 36 måneder")
    val antallÅrI36Måneder = Opplysningstype.somDesimaltall("Antall år i 36 måneder")
    val inntektperiode1 = Opplysningstype.somBeløp("Inntektperiode 1")
    val inntektperiode2 = Opplysningstype.somBeløp("Inntektperiode 2")
    val inntektperiode3 = Opplysningstype.somBeløp("Inntektperiode 3")

    val avkortetperiode1 = Opplysningstype.somBeløp("Avkortet inntektperiode 1")
    val avkortetperiode2 = Opplysningstype.somBeløp("Avkortet inntektperiode 2")
    val avkortetperiode3 = Opplysningstype.somBeløp("Avkortet inntektperiode 3")

    regel(inntektperiode1) { summerPeriode(inntekt, SummerPeriode.InntektPeriode.Første) }
    regel(inntektperiode2) { summerPeriode(inntekt, SummerPeriode.InntektPeriode.Andre) }
    regel(inntektperiode3) { summerPeriode(inntekt, SummerPeriode.InntektPeriode.Tredje) }

    regel(avkortetperiode1) { minstAv(inntektperiode1, maksgrenseForGrunnlag) }
    regel(avkortetperiode2) { minstAv(inntektperiode2, maksgrenseForGrunnlag) }
    regel(avkortetperiode3) { minstAv(inntektperiode3, maksgrenseForGrunnlag) }

    regel(beløpSiste36) { sumAv(avkortetperiode1, avkortetperiode2, avkortetperiode3) }
    regel(antallÅrI36Måneder) { oppslag(Dagpengegrunnlag.søknadstidspunkt) { 3.0 } }
    regel(årligGjennomsnittSiste36) { divisjon(beløpSiste36, antallÅrI36Måneder) }
    return årligGjennomsnittSiste36
}

private fun grunnbeløpFor(it: LocalDate) =
    getGrunnbeløpForRegel(no.nav.dagpenger.grunnbelop.Regel.Grunnlag)
        .forDato(it)
        .verdi
        .let { Beløp(it) }
