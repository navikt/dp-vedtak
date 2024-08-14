package no.nav.dagpenger.regel.fastsetting

import no.nav.dagpenger.opplysning.Opplysningstype
import no.nav.dagpenger.opplysning.Regelsett
import no.nav.dagpenger.opplysning.regel.brukt
import no.nav.dagpenger.opplysning.regel.erUlik
import no.nav.dagpenger.opplysning.regel.høyesteAv
import no.nav.dagpenger.opplysning.verdier.Beløp
import no.nav.dagpenger.opplysning.verdier.Inntekt
import no.nav.dagpenger.regel.Verneplikt

object Dagpengegrunnlag {
    val inntekt = Opplysningstype.somInntekt("Inntekt")
    val oppjustertinntekt = Opplysningstype.somInntekt("Oppjustert inntekt")
    val verneplikt = Verneplikt.vurderingAvVerneplikt
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
            val siste12 = siste12mnd(inntekt)
            val siste36 = siste36mnd(inntekt)
            regel(grunnlag) { høyesteAv(siste12, siste36) }
            regel(harAvkortet) { erUlik(avkortet, uavkortet) }
            regel(beregningsregel) { brukt(grunnlag) }
        }
}

private fun Regelsett.siste12mnd(inntekt: Opplysningstype<Inntekt>): Opplysningstype<Beløp> {
    val sum = Opplysningstype.somBeløp("Sum inntekt 12 mnd")
    val avkortet = Opplysningstype.somBeløp("dsfasdfjlk")

    regel(avkortet) { høyesteAv(sum) }
    val grunnlag = Opplysningstype.somBeløp("Inntektsgrunnlag 12 mnd")

    regel(grunnlag) { høyesteAv(avkortet) }
    return grunnlag
}

private fun Regelsett.siste36mnd(inntekt: Opplysningstype<Inntekt>): Opplysningstype<Beløp> =
    Opplysningstype.somBeløp("Inntektsgrunnlag 36 mnd")
