package no.nav.dagpenger.opplysning

import io.kotest.assertions.throwables.shouldNotThrow
import io.kotest.matchers.shouldBe
import no.nav.dagpenger.opplysning.regel.alle
import kotlin.test.Test

class RegelTest {
    @Test
    fun `Regel produserer bare nye opplysningen hvis det den avhenger av er nyere enn det den har produsert`() {
        val a = Opplysningstype.somBoolsk("A")
        val b = Opplysningstype.somBoolsk("B")
        val c = Opplysningstype.somBoolsk("C")
        val regelsett =
            Regelsett("regelsett") {
                regel(c) {
                    alle(a, b)
                }
            }

        val opplysninger = Opplysninger()
        val regelkjøring1 = Regelkjøring(1.mai, opplysninger, regelsett)
        val opplysning = Faktum(a, true, Gyldighetsperiode(1.januar, 2.mai))
        opplysninger.leggTil(opplysning)
        opplysninger.leggTil(Faktum(b, true))
        regelkjøring1.evaluer()
        opplysninger.finnOpplysning(c).verdi shouldBe true

        // Første forsøk på å fastsette A var feil.
        val regelkjøring2 = Regelkjøring(1.mai, opplysninger, regelsett)
        opplysninger.erstatt(opplysning, Faktum(a, false, Gyldighetsperiode(1.januar, 2.mai)))
        shouldNotThrow<IllegalArgumentException> { regelkjøring2.evaluer() }

        opplysninger.finnOpplysning(c).verdi shouldBe false
    }
}
