package no.nav.dagpenger.opplysning.regelsett

import no.nav.dagpenger.opplysning.Opplysningstype
import no.nav.dagpenger.opplysning.Regelsett
import no.nav.dagpenger.opplysning.id
import no.nav.dagpenger.opplysning.regel.enAv
import no.nav.dagpenger.opplysning.regel.innhentMed
import no.nav.dagpenger.opplysning.regel.multiplikasjon
import no.nav.dagpenger.opplysning.regel.oppslag
import no.nav.dagpenger.opplysning.regel.størreEnnEllerLik
import java.time.LocalDate

internal object Minsteinntekt {
    val antallG12mndInntekt = Opplysningstype.somDesimaltall("Antall G for krav til 12 mnd inntekt")
    val antallG36mndInntekt = Opplysningstype.somDesimaltall("Antall G for krav til 36 mnd inntekt")
    val inntekt12 = Opplysningstype.somDesimaltall("Inntekt siste 12 mnd".id("inntekt12mnd"))
    val inntekt36 = Opplysningstype.somDesimaltall("Inntekt siste 36 mnd".id("inntekt36mnd"))
    val grunnbeløp = Opplysningstype.somDesimaltall("Grunnbeløp")

    private val virkningsdato = Virkningsdato.virkningsdato
    private val antattRapporteringsFrist = Opplysningstype.somDato("Antatt rapporteringsfrist")
    private val reellRapporteringsFrist = Opplysningstype.somDato("Reell rapporteringsfrist")
    private val sisteAvsluttendeKalenderMåned = Opplysningstype.somDato("Siste avsluttendende kalendermåned")
    private val førsteAvsluttendeKalenderMåned = Opplysningstype.somDato("Første kalendermåned")
    private val inntektId = Opplysningstype.somUlid("InntektId")

    private val nedreTerskel = Opplysningstype.somDesimaltall("Inntektskrav for siste 12 mnd")
    private val øvreTerskel = Opplysningstype.somDesimaltall("Inntektskrav for siste 36 mnd")
    private val overNedreTerskel = Opplysningstype.somBoolsk("Inntekt er over kravet for siste 12 mnd")
    private val overØvreTerskel = Opplysningstype.somBoolsk("Inntekt er over kravet for siste 36 mnd")

    val minsteinntekt = Opplysningstype.somBoolsk("Minsteinntekt")

    val regelsett =
        Regelsett("Minsteinntekt") {
            regel(antallG12mndInntekt) { oppslag(virkningsdato) { 1.5 } }
            regel(antallG36mndInntekt) { oppslag(virkningsdato) { 3.0 } }
            regel(inntekt12) { innhentMed(virkningsdato) }
            regel(inntekt36) { innhentMed(virkningsdato) }
            regel(grunnbeløp) { oppslag(virkningsdato) { Grunnbeløp.finnFor(it) } }
            regel(nedreTerskel) { multiplikasjon(antallG12mndInntekt, grunnbeløp) }
            regel(øvreTerskel) { multiplikasjon(antallG36mndInntekt, grunnbeløp) }
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
