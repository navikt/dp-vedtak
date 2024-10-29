package no.nav.dagpenger.opplysning.regel

import io.kotest.matchers.shouldBe
import no.nav.dagpenger.opplysning.Faktum
import no.nav.dagpenger.opplysning.Opplysninger
import no.nav.dagpenger.opplysning.Regelkjøring
import no.nav.dagpenger.opplysning.Regelsett
import no.nav.dagpenger.opplysning.TestOpplysningstyper.a
import no.nav.dagpenger.opplysning.TestOpplysningstyper.b
import no.nav.dagpenger.opplysning.mai
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class ErUsannTest {
    private val regelsett =
        Regelsett("Test") {
            regel(a) { innhentes }
            regel(b) { erUsann(a) }
        }
    private val opplysninger = Opplysninger()
    private lateinit var regelkjøring: Regelkjøring

    @BeforeEach
    fun setup() {
        regelkjøring = Regelkjøring(23.mai(2024), opplysninger, regelsett)
    }

    @Test
    fun `Opplysning er ikke sann`() {
        opplysninger.leggTil(Faktum(a, false)).also { regelkjøring.evaluer() }
        opplysninger.har(b) shouldBe true
        opplysninger.finnOpplysning(b).verdi shouldBe true
    }

    @Test
    fun `Opplysning er sann`() {
        opplysninger.leggTil(Faktum(a, true)).also { regelkjøring.evaluer() }
        opplysninger.har(b) shouldBe true
        opplysninger.finnOpplysning(b).verdi shouldBe false
    }
}
