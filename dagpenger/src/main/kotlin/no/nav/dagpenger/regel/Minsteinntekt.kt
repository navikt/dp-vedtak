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
import no.nav.dagpenger.regel.GrenseverdierForMinsteArbeidsinntekt.finnTerskel

object Minsteinntekt {
    private val `12mndTerskelFaktor` = Opplysningstype.somDesimaltall("Antall G for krav til 12 mnd arbeidsinntekt")
    private val `36mndTerskelFaktor` = Opplysningstype.somDesimaltall("Antall G for krav til 36 mnd arbeidsinntekt")
    val inntekt12 = Opplysningstype.somDesimaltall("Arbeidsinntekt siste 12 mnd".id("InntektSiste12Mnd"))
    val inntekt36 = Opplysningstype.somDesimaltall("Arbeidsinntekt siste 36 mnd".id("InntektSiste36Mnd"))
    private val grunnbeløp = Opplysningstype.somDesimaltall("Grunnbeløp")

    private val virkningsdato = Virkningsdato.virkningsdato
    private val `12mndTerskel` = Opplysningstype.somDesimaltall("Inntektskrav for siste 12 mnd")
    private val `36mndTerskel` = Opplysningstype.somDesimaltall("Inntektskrav for siste 36 mnd")
    private val over12mndTerskel = Opplysningstype.somBoolsk("Arbeidsinntekt er over kravet for siste 12 mnd")
    private val over36mndTerskel = Opplysningstype.somBoolsk("Arbeidsinntekt er over kravet for siste 36 mnd")

    val minsteinntekt = Opplysningstype.somBoolsk("Krav til minsteinntekt")

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
            regel(`12mndTerskelFaktor`) { oppslag(virkningsdato) { finnTerskel(it).nedre } }
            regel(`36mndTerskelFaktor`) { oppslag(virkningsdato) { finnTerskel(it).øvre } }
            regel(`12mndTerskel`) { multiplikasjon(`12mndTerskelFaktor`, grunnbeløp) }
            regel(`36mndTerskel`) { multiplikasjon(`36mndTerskelFaktor`, grunnbeløp) }
            regel(over12mndTerskel) { størreEnnEllerLik(inntekt12, `12mndTerskel`) }
            regel(over36mndTerskel) { størreEnnEllerLik(inntekt36, `36mndTerskel`) }
            regel(minsteinntekt) { enAv(over12mndTerskel, over36mndTerskel) }
        }
}
