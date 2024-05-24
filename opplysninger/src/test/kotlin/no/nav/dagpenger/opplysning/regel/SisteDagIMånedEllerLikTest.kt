package no.nav.dagpenger.opplysning.regel

import no.nav.dagpenger.opplysning.Faktum
import no.nav.dagpenger.opplysning.Opplysninger
import no.nav.dagpenger.opplysning.Regelkjøring
import no.nav.dagpenger.opplysning.Regelsett
import no.nav.dagpenger.opplysning.TestOpplysningstyper.boolskA
import no.nav.dagpenger.opplysning.TestOpplysningstyper.faktorA
import no.nav.dagpenger.opplysning.TestOpplysningstyper.faktorB
import no.nav.dagpenger.opplysning.mai
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class SisteDagIMånedEllerLikTest {
    private val opplysninger = Opplysninger()
    private val regelkjøring =
        Regelkjøring(
            1.mai,
            opplysninger,
            Regelsett("regelsett") {
                regel(boolskA) { størreEnnEllerLik(faktorA, faktorB) }
            },
        )

    @Test
    fun `større enn`() {
        opplysninger.leggTil(Faktum(faktorA, 2.0))
        opplysninger.leggTil(Faktum(faktorB, 1.0))
        val utledet = opplysninger.finnOpplysning(boolskA)
        assertTrue(utledet.verdi)
    }

    @Test
    fun `ikke større enn`() {
        opplysninger.leggTil(Faktum(faktorA, 2.0))
        opplysninger.leggTil(Faktum(faktorB, 1.0))
        val utledet = opplysninger.finnOpplysning(boolskA)
        assertTrue(utledet.verdi)
    }
}
