package no.nav.dagpenger.opplysning.regel

import io.kotest.matchers.shouldBe
import no.nav.dagpenger.opplysning.Faktum
import no.nav.dagpenger.opplysning.Opplysninger
import no.nav.dagpenger.opplysning.Opplysningstype
import no.nav.dagpenger.opplysning.Regelsett
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

class RegelTest {
    @Test
    fun `kaster feil når opplysninger mangler`() {
        val a = Opplysningstype.somBoolsk("a")
        val b = Opplysningstype.somBoolsk("b")
        val c = Opplysningstype.somBoolsk("c")
        val regelsett =
            Regelsett("test") {
                regel(a) { innhentMed(b, c) }
            }

        val exception: IllegalArgumentException =
            assertThrows<IllegalArgumentException> {
                regelsett.regler().first().lagProdukt(Opplysninger(listOf(Faktum(b, false))))
            }

        exception.message shouldBe
            """
            |Prøver å kjøre Ekstern(a), men mangler avhengigheter.
            |Det er mismatch mellom lagPlan() og lagProdukt().
            |- Avhengigheter vi mangler: c
            |- Avhengigheter vi trenger: b, c
            |- Avhengigheter vi fant: b
            """.trimMargin()
    }
}
