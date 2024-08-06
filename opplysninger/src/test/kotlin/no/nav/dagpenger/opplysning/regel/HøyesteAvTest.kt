package no.nav.dagpenger.opplysning.regel

import io.kotest.matchers.shouldBe
import no.nav.dagpenger.opplysning.Faktum
import no.nav.dagpenger.opplysning.Opplysninger
import no.nav.dagpenger.opplysning.Opplysningstype
import no.nav.dagpenger.opplysning.Regelkjøring
import no.nav.dagpenger.opplysning.Regelsett
import no.nav.dagpenger.opplysning.mai
import no.nav.dagpenger.opplysning.verdier.Stønadsperiode
import kotlin.test.Test

class HøyesteAvTest {
    private companion object {
        private val opplysning1 = Opplysningstype.somStønadsperiode("opplysning1")
        private val opplysning2 = Opplysningstype.somStønadsperiode("opplysning2")
        private val høyeste = Opplysningstype.somStønadsperiode("høyeste")
    }

    @Test
    fun `høyeste av`() {
        val regelsett =
            Regelsett("Test") {
                regel(høyeste) { høyeste.høyesteAv(opplysning1, opplysning2) }
            }
        val opplysninger =
            Opplysninger().also {
                Regelkjøring(23.mai(2024), opplysninger = it, regelsett)
            }

        opplysninger.leggTil(Faktum(opplysning1, Stønadsperiode(1)))
        opplysninger.leggTil(Faktum(opplysning2, Stønadsperiode(2)))
        opplysninger.har(høyeste) shouldBe true
        opplysninger.finnOpplysning(høyeste).verdi shouldBe Stønadsperiode(2)
    }
}
