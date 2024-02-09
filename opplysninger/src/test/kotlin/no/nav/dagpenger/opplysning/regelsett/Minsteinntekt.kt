package no.nav.dagpenger.opplysning.regelsett

import no.nav.dagpenger.opplysning.Opplysningstype
import no.nav.dagpenger.opplysning.Regelsett
import no.nav.dagpenger.opplysning.id
import no.nav.dagpenger.opplysning.regel.enAv
import no.nav.dagpenger.opplysning.regel.multiplikasjon
import no.nav.dagpenger.opplysning.regel.oppslag
import no.nav.dagpenger.opplysning.regel.størreEnnEllerLik
import java.time.LocalDate

object Minsteinntekt {
    val nedreTerskelFaktor = Opplysningstype<Double>("Antall G for krav til 12 mnd inntekt")
    val øvreTerskelFaktor = Opplysningstype<Double>("Antall G for krav til 36 mnd inntekt")
    val inntekt12 = Opplysningstype<Double>("Inntekt siste 12 mnd".id("inntekt12mnd"))
    val inntekt36 = Opplysningstype<Double>("Inntekt siste 36 mnd".id("inntekt36mnd"))
    val grunnbeløp = Opplysningstype<Double>("Grunnbeløp")

    private val virkningsdato = Virkningsdato.virkningsdato

    val nedreTerskel = Opplysningstype<Double>("Inntektskrav for siste 12 mnd")
    val øvreTerskel = Opplysningstype<Double>("Inntektskrav for siste 36 mnd")

    val overNedreTerskel = Opplysningstype<Boolean>("Inntekt er over kravet for siste 12 mnd")
    val overØvreTerskel = Opplysningstype<Boolean>("Inntekt er over kravet for siste 36 mnd")

    val minsteinntekt = Opplysningstype<Boolean>("Minsteinntekt")

    val regelsett =
        Regelsett("Minsteinntekt") {
            regel(grunnbeløp) { oppslag(virkningsdato) { Grunnbeløp.finnFor(it) } }
            // regel(nedreTerskelFaktor) { oppslag(virkningsdato) { 1.5 } }
            // regel(øvreTerskelFaktor) { oppslag(virkningsdato) { 3.5 } }
            regel(nedreTerskel) { multiplikasjon(nedreTerskelFaktor, grunnbeløp) }
            regel(øvreTerskel) { multiplikasjon(øvreTerskelFaktor, grunnbeløp) }
            regel(overNedreTerskel) { størreEnnEllerLik(inntekt12, nedreTerskel) }
            regel(overØvreTerskel) { størreEnnEllerLik(inntekt36, øvreTerskel) }
            regel(overØvreTerskel) { størreEnnEllerLik(inntekt36, øvreTerskel) }
            regel(overØvreTerskel) { størreEnnEllerLik(inntekt36, øvreTerskel) }
            regel(minsteinntekt) { enAv(overNedreTerskel, overØvreTerskel) }
        }

    internal object Grunnbeløp {
        const val TEST_GRUNNBELØP = 118620.0

        fun finnFor(dato: LocalDate) = TEST_GRUNNBELØP
    }
}
