package no.nav.dagpenger.behandling.konklusjon

import io.kotest.matchers.shouldBe
import no.nav.dagpenger.opplysning.Faktum
import no.nav.dagpenger.opplysning.Opplysninger
import no.nav.dagpenger.opplysning.Opplysningstype
import no.nav.dagpenger.opplysning.Regelsett
import no.nav.dagpenger.opplysning.regel.enAv
import no.nav.dagpenger.opplysning.regel.erSann
import org.junit.jupiter.api.Test
import kotlin.time.measureTime

class KonklusjonTest {
    private companion object {
        val saksopplysningAlder = Opplysningstype.somBoolsk("saksopplysningAlder")
        val saksopplysningInntekt = Opplysningstype.somBoolsk("saksopplysningInntekt")
        val avslagPåAlder = Opplysningstype.somBoolsk("avslagPåAlder")
        val avslagPåInntekt = Opplysningstype.somBoolsk("avslagPåInntekt")
        val knockoutAvslag = Opplysningstype.somBoolsk("knockoutAvslag")
    }

    private val kontrollpunkt1 =
        Regelsett("AvslagPåAlder") {
            regel(avslagPåAlder) { erSann(saksopplysningAlder) }
        }
    private val kontrollpunkt2 =
        Regelsett("AvslagPåInntekt") {
            regel(avslagPåInntekt) { erSann(saksopplysningInntekt) }
        }

    private val kontrollpunktKnockout =
        Regelsett("Knockout") {
            regel(knockoutAvslag) { enAv(avslagPåAlder, avslagPåInntekt) }
        }

    @Test
    fun `når ingen opplysninger er til stede kan vi ikke lage konklusjon`() {
        val saksopplysninger = Opplysninger()

        measureTime { }
        val konklusjon = Konklusjon(saksopplysninger, kontrollpunkt1, kontrollpunkt2, kontrollpunktKnockout)
        konklusjon.kanKonkludere(knockoutAvslag) shouldBe false
    }

    @Test
    fun `når alle opplysninger er til stede kan vi lage konklusjon`() {
        val saksopplysninger =
            Opplysninger(
                listOf(
                    Faktum(saksopplysningAlder, true),
                    Faktum(saksopplysningInntekt, true),
                ),
            )

        val konklusjon = Konklusjon(saksopplysninger, kontrollpunkt1, kontrollpunkt2, kontrollpunktKnockout)
        konklusjon.kanKonkludere(knockoutAvslag) shouldBe true
    }

    @Test
    fun `alder og minsteinntekt må være avklart for å konkludere`() {
        val saksopplysninger = Opplysninger(listOf(Faktum(saksopplysningAlder, true)))

        val konklusjon = Konklusjon(saksopplysninger, kontrollpunkt1, kontrollpunkt2, kontrollpunktKnockout)
        konklusjon.kanKonkludere(knockoutAvslag) shouldBe false
    }

    @Test
    fun `minst en av alder og minsteinntekt må ha negativt utfall for å konkludere`() {
        val saksopplysninger =
            Opplysninger(
                listOf(
                    Faktum(saksopplysningAlder, true),
                    Faktum(saksopplysningInntekt, false),
                ),
            )

        val konklusjon = Konklusjon(saksopplysninger, kontrollpunkt1, kontrollpunkt2, kontrollpunktKnockout)
        konklusjon.kanKonkludere(knockoutAvslag) shouldBe true
    }
}
