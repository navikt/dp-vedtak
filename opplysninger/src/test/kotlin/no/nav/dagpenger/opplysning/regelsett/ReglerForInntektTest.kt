package no.nav.dagpenger.opplysning.regelsett

import no.nav.dagpenger.opplysning.Opplysningstype
import no.nav.dagpenger.opplysning.Regelsett
import no.nav.dagpenger.opplysning.id
import no.nav.dagpenger.opplysning.regel.dato.førsteArbeidsdag
import no.nav.dagpenger.opplysning.regel.enAv
import no.nav.dagpenger.opplysning.regel.innhentMed
import no.nav.dagpenger.opplysning.regel.multiplikasjon
import no.nav.dagpenger.opplysning.regel.oppslag
import no.nav.dagpenger.opplysning.regel.størreEnnEllerLik
import no.nav.dagpenger.opplysning.verdier.Beløp
import java.time.LocalDate

internal object ReglerForInntektTest {
    val antallG12mndInntekt = Opplysningstype.somDesimaltall("Antall G for krav til 12 mnd inntekt")
    val antallG36mndInntekt = Opplysningstype.somDesimaltall("Antall G for krav til 36 mnd inntekt")
    val inntekt12 = Opplysningstype.somBeløp("Inntekt siste 12 mnd".id("inntekt12mnd"))
    val inntekt36 = Opplysningstype.somBeløp("Inntekt siste 36 mnd".id("inntekt36mnd"))
    val grunnbeløp = Opplysningstype.somBeløp("Grunnbeløp")

    private val virkningsdato = Virkningsdato.virkningsdato
    private val antattRapporteringsFrist = Opplysningstype.somDato("Antatt rapporteringsfrist")
    private val reellRapporteringsFrist = Opplysningstype.somDato("Reell rapporteringsfrist")
    private val sisteAvsluttendeKalenderMåned = Opplysningstype.somDato("Siste avsluttendende kalendermåned")
    private val førsteAvsluttendeKalenderMåned = Opplysningstype.somDato("Første kalendermåned")
    private val inntektId = Opplysningstype.somUlid("InntektId")

    private val nedreTerskel = Opplysningstype.somBeløp("Inntektskrav for siste 12 mnd")
    private val øvreTerskel = Opplysningstype.somBeløp("Inntektskrav for siste 36 mnd")
    private val overNedreTerskel = Opplysningstype.somBoolsk("Inntekt er over kravet for siste 12 mnd")
    private val overØvreTerskel = Opplysningstype.somBoolsk("Inntekt er over kravet for siste 36 mnd")

    val minsteinntekt = Opplysningstype.somBoolsk("Minsteinntekt")

    val regelsett =
        Regelsett("Minsteinntekt") {
            regel(antattRapporteringsFrist) { oppslag(virkningsdato) { LocalDate.of(it.year, it.month, 5) } }
            regel(reellRapporteringsFrist) { førsteArbeidsdag(antattRapporteringsFrist) }
            regel(antallG12mndInntekt) { oppslag(virkningsdato) { 1.5 } }
            regel(antallG36mndInntekt) { oppslag(virkningsdato) { 3.0 } }
            regel(inntekt12) { innhentMed(virkningsdato) }
            regel(inntekt36) { innhentMed(virkningsdato) }
            regel(grunnbeløp) { oppslag(virkningsdato) { Grunnbeløp.finnFor(it) } }
            regel(nedreTerskel) { multiplikasjon(grunnbeløp, antallG12mndInntekt) }
            regel(øvreTerskel) { multiplikasjon(grunnbeløp, antallG36mndInntekt) }
            regel(overNedreTerskel) { størreEnnEllerLik(inntekt12, nedreTerskel) }
            regel(overØvreTerskel) { størreEnnEllerLik(inntekt36, øvreTerskel) }
            regel(minsteinntekt) { enAv(overNedreTerskel, overØvreTerskel) }
        }
}

internal object Grunnbeløp {
    val TEST_GRUNNBELØP = Beløp(118620.0)
    private val grunnbeløp =
        mapOf(
            LocalDate.of(2020, 5, 1) to Beløp(99858.0),
            LocalDate.of(2021, 5, 1) to Beløp(105144.0),
            LocalDate.of(2022, 5, 1) to TEST_GRUNNBELØP,
        )

    fun finnFor(dato: LocalDate) =
        grunnbeløp
            .filterKeys { it <= dato }
            .maxByOrNull { it.key }
            ?.value
            ?: throw IllegalArgumentException("Fant ikke grunnbeløp for $dato")
}
