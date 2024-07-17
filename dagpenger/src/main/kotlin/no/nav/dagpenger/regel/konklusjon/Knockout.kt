package no.nav.dagpenger.regel.konklusjon

import no.nav.dagpenger.opplysning.Opplysningstype
import no.nav.dagpenger.opplysning.Regelsett
import no.nav.dagpenger.opplysning.regel.enAv
import no.nav.dagpenger.opplysning.regel.ikkeOppfylt
import no.nav.dagpenger.regel.Alderskrav.kravTilAlder
import no.nav.dagpenger.regel.Minsteinntekt.minsteinntekt

private val avslagPåAlder = Opplysningstype.somBoolsk("Kan avslå på alder")
private val avslagPåInntekt = Opplysningstype.somBoolsk("Kan avslå på inntekt")
val knockoutAvslag = Opplysningstype.somBoolsk("Kan avslå tidlig")

private val avslagAlder =
    Regelsett("AvslagPåAlder") {
        regel(avslagPåAlder) { ikkeOppfylt(kravTilAlder) }
    }
private val avslagInntekt =
    Regelsett("AvslagPåInntekt") {
        regel(avslagPåInntekt) { ikkeOppfylt(minsteinntekt) }
    }

val knockout =
    Regelsett("Knockout") {
        regel(knockoutAvslag) { enAv(avslagPåAlder, avslagPåInntekt) }
    }

val regelsettKnockout = listOf(avslagAlder, avslagInntekt, knockout)
