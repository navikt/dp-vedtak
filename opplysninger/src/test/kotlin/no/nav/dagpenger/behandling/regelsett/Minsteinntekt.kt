package no.nav.dagpenger.behandling.regelsett

import no.nav.dagpenger.behandling.Opplysningstype
import no.nav.dagpenger.behandling.Regelsett
import no.nav.dagpenger.behandling.regel.enAv
import no.nav.dagpenger.behandling.regel.multiplikasjon
import no.nav.dagpenger.behandling.regel.oppslag
import no.nav.dagpenger.behandling.regel.størreEnnEllerLik
import java.time.LocalDate

object Minsteinntekt {
    val nedreTerskelFaktor = Opplysningstype<Double>("Antall G for krav til 12 mnd inntekt")
    val øvreTerskelFaktor = Opplysningstype<Double>("Antall G for krav til 36 mnd inntekt")
    val inntekt12 = Opplysningstype<Double>("Inntekt siste 12 mnd")
    val inntekt36 = Opplysningstype<Double>("Inntekt siste 36 mnd")
    val grunnbeløp = Opplysningstype<Double>("Grunnbeløp")
    val virkningsdato = Opplysningstype<LocalDate>("Virkningsdato")

    val nedreTerskel = Opplysningstype<Double>("Inntektskrav for siste 12 mnd")
    val øvreTerskel = Opplysningstype<Double>("Inntektskrav for siste 36 mnd")

    val overNedreTerskel = Opplysningstype<Boolean>("Inntekt er over kravet for siste 12 mnd")
    val overØvreTerskel = Opplysningstype<Boolean>("Inntekt er over kravet for siste 36 mnd")

    val minsteinntekt = Opplysningstype<Boolean>("Minsteinntekt")

    val regelsett =
        Regelsett("Minsteinntekt").apply {
            oppslag(grunnbeløp, virkningsdato) { Grunnbeløp.finnFor(it) }
            multiplikasjon(nedreTerskel, nedreTerskelFaktor, grunnbeløp)
            multiplikasjon(øvreTerskel, øvreTerskelFaktor, grunnbeløp)
            størreEnnEllerLik(overNedreTerskel, inntekt12, nedreTerskel)
            størreEnnEllerLik(overØvreTerskel, inntekt36, øvreTerskel)
            enAv(minsteinntekt, overNedreTerskel, overØvreTerskel)
        }

    internal object Grunnbeløp {
        const val TEST_GRUNNBELØP = 118620.0

        fun finnFor(dato: LocalDate) = TEST_GRUNNBELØP
    }
}
