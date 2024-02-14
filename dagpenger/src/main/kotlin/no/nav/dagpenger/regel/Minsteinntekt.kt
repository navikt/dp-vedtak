package no.nav.dagpenger.regel

import no.nav.dagpenger.grunnbelop.Regel
import no.nav.dagpenger.grunnbelop.forDato
import no.nav.dagpenger.grunnbelop.getGrunnbeløpForRegel
import no.nav.dagpenger.opplysning.Opplysningstype
import no.nav.dagpenger.opplysning.Regelsett
import no.nav.dagpenger.opplysning.id
import no.nav.dagpenger.opplysning.regel.enAv
import no.nav.dagpenger.opplysning.regel.innhentMed
import no.nav.dagpenger.opplysning.regel.multiplikasjon
import no.nav.dagpenger.opplysning.regel.oppslag
import no.nav.dagpenger.opplysning.regel.størreEnnEllerLik

object Minsteinntekt {
    val nedreTerskelFaktor = Opplysningstype.somDesimaltall("Antall G for krav til 12 mnd inntekt")
    val øvreTerskelFaktor = Opplysningstype.somDesimaltall("Antall G for krav til 36 mnd inntekt")
    val inntekt12 = Opplysningstype.somDesimaltall("Inntekt siste 12 mnd".id("InntektSiste12Mnd"))
    val inntekt36 = Opplysningstype.somDesimaltall("Inntekt siste 36 mnd".id("InntektSiste3År"))
    val grunnbeløp = Opplysningstype.somDesimaltall("Grunnbeløp")

    private val virkningsdato = Virkningsdato.virkningsdato
    private val nedreTerskel = Opplysningstype.somDesimaltall("Inntektskrav for siste 12 mnd")
    private val øvreTerskel = Opplysningstype.somDesimaltall("Inntektskrav for siste 36 mnd")
    private val overNedreTerskel = Opplysningstype.somBoolsk("Inntekt er over kravet for siste 12 mnd")
    private val overØvreTerskel = Opplysningstype.somBoolsk("Inntekt er over kravet for siste 36 mnd")

    val minsteinntekt = Opplysningstype.somBoolsk("Minsteinntekt")

    val regelsett =
        Regelsett("Minsteinntekt") {
            regel(inntekt12) { innhentMed(virkningsdato) }
            regel(inntekt36) { innhentMed(virkningsdato) }
            regel(grunnbeløp) {
                oppslag(virkningsdato) {
                    getGrunnbeløpForRegel(Regel.Minsteinntekt).forDato(it).verdi
                        // TODO: Bli enige med oss selv hva som er Double og BigDecimal
                        .toDouble()
                }
            }
            regel(nedreTerskelFaktor) { oppslag(virkningsdato) { GrenseverdierForMinsteArbeidsinntekt.finnFaktor(it).nedre } }
            regel(øvreTerskelFaktor) { oppslag(virkningsdato) { GrenseverdierForMinsteArbeidsinntekt.finnFaktor(it).øvre } }
            regel(nedreTerskel) { multiplikasjon(nedreTerskelFaktor, grunnbeløp) }
            regel(øvreTerskel) { multiplikasjon(øvreTerskelFaktor, grunnbeløp) }
            regel(overNedreTerskel) { størreEnnEllerLik(inntekt12, nedreTerskel) }
            regel(overØvreTerskel) { størreEnnEllerLik(inntekt36, øvreTerskel) }
            regel(minsteinntekt) { enAv(overNedreTerskel, overØvreTerskel) }
        }
}
