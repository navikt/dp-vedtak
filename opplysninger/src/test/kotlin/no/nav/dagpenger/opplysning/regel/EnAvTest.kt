package no.nav.dagpenger.opplysning.regel

import io.kotest.matchers.shouldBe
import no.nav.dagpenger.opplysning.Faktum
import no.nav.dagpenger.opplysning.Opplysninger
import no.nav.dagpenger.opplysning.Regelkjøring
import no.nav.dagpenger.opplysning.Regelsett
import no.nav.dagpenger.opplysning.TestOpplysningstyper.boolskA
import no.nav.dagpenger.opplysning.TestOpplysningstyper.boolskB
import no.nav.dagpenger.opplysning.TestOpplysningstyper.boolskC
import no.nav.dagpenger.opplysning.mai
import org.junit.jupiter.api.Test

internal class EnAvTest {
    private val opplysninger = Opplysninger()
    private val regelkjøring =
        Regelkjøring(
            1.mai,
            opplysninger,
            Regelsett("regelsett") {
                regel(boolskA) { enAv(boolskB, boolskC) }
            },
        )

    @Test
    fun `hvis en av opplysningene er sanne så er utledningen sann`() {
        opplysninger.leggTil(Faktum(boolskB, false))
        opplysninger.leggTil(Faktum(boolskC, true))
        val utledet = opplysninger.finnOpplysning(boolskA)
        utledet.verdi shouldBe true
    }

    @Test
    fun `hvis ingen av opplysningene er sanne så er utledningen usann`() {
        opplysninger.leggTil(Faktum(boolskB, false))
        opplysninger.leggTil(Faktum(boolskC, false))
        val utledet = opplysninger.finnOpplysning(boolskA)
        utledet.verdi shouldBe false
    }

    @Test
    fun `hvis begge opplysningene er sanne så er utledningen sann`() {
        opplysninger.leggTil(Faktum(boolskB, true))
        opplysninger.leggTil(Faktum(boolskC, true))
        val utledet = opplysninger.finnOpplysning(boolskA)
        utledet.verdi shouldBe true
        utledet.verdi shouldBe true
    }
}
