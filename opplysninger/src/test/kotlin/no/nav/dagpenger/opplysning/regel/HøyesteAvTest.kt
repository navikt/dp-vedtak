package no.nav.dagpenger.opplysning.regel

import io.kotest.matchers.shouldBe
import no.nav.dagpenger.opplysning.Faktum
import no.nav.dagpenger.opplysning.Opplysninger
import no.nav.dagpenger.opplysning.Opplysningstype
import no.nav.dagpenger.opplysning.Regelkjøring
import no.nav.dagpenger.opplysning.Regelsett
import no.nav.dagpenger.opplysning.mai
import kotlin.test.Test

class HøyesteAvTest {
    private companion object {
        private val opplysning1 = Opplysningstype.somHeltall("opplysning1")
        private val opplysning2 = Opplysningstype.somHeltall("opplysning2")
        private val høyeste = Opplysningstype.somHeltall("høyeste")
    }

    @Test
    fun `høyeste av`() {
        val regelsett =
            Regelsett("Test") {
                regel(høyeste) { høyeste.høyesteAv(opplysning1, opplysning2) }
            }
        val opplysninger = Opplysninger()
        val regelkjøring = Regelkjøring(23.mai(2024), opplysninger, regelsett)

        opplysninger.leggTil(Faktum(opplysning1, 1))
        opplysninger.leggTil(Faktum(opplysning2, 2))
        opplysninger.har(høyeste) shouldBe true
        opplysninger.finnOpplysning(høyeste).verdi shouldBe 2
    }
}
