package no.nav.dagpenger.regel

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
    private val nedreTerskel = Opplysningstype<Double>("Inntektskrav for siste 12 mnd")
    private val øvreTerskel = Opplysningstype<Double>("Inntektskrav for siste 36 mnd")
    private val overNedreTerskel = Opplysningstype<Boolean>("Inntekt er over kravet for siste 12 mnd")
    private val overØvreTerskel = Opplysningstype<Boolean>("Inntekt er over kravet for siste 36 mnd")

    val minsteinntekt = Opplysningstype<Boolean>("Minsteinntekt")

    val regelsett =
        Regelsett("Minsteinntekt") {
            regel(grunnbeløp) { oppslag(virkningsdato) { Grunnbeløp.finnFor(it) } }
            regel(nedreTerskel) { multiplikasjon(nedreTerskelFaktor, grunnbeløp) }
            regel(øvreTerskel) { multiplikasjon(øvreTerskelFaktor, grunnbeløp) }
            regel(overNedreTerskel) { størreEnnEllerLik(inntekt12, nedreTerskel) }
            regel(overØvreTerskel) { størreEnnEllerLik(inntekt36, øvreTerskel) }
            regel(minsteinntekt) { enAv(overNedreTerskel, overØvreTerskel) }
        }
}

internal object Grunnbeløp {
    const val TEST_GRUNNBELØP = 118620.0
    private val grunnbeløp =
        mapOf(
            LocalDate.of(2020, 5, 1) to 99858.0,
            LocalDate.of(2021, 5, 1) to 105144.0,
            LocalDate.of(2022, 5, 1) to TEST_GRUNNBELØP,
        )

    fun finnFor(dato: LocalDate) =
        grunnbeløp.filterKeys { it <= dato }.maxByOrNull { it.key }?.value
            ?: throw IllegalArgumentException("Fant ikke grunnbeløp for $dato")
}
