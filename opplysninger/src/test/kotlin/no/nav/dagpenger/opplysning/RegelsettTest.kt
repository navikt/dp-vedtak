package no.nav.dagpenger.opplysning

import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import no.nav.dagpenger.opplysning.TestOpplysningstyper.faktorA
import no.nav.dagpenger.opplysning.TestOpplysningstyper.faktorB
import no.nav.dagpenger.opplysning.TestOpplysningstyper.grunntall
import no.nav.dagpenger.opplysning.TestOpplysningstyper.produkt
import no.nav.dagpenger.opplysning.regel.multiplikasjon
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class RegelsettTest {
    private val regelsett
        get() =
            Regelsett("regelsett") {
                regel(produkt, 1.januar) { multiplikasjon(grunntall, faktorA) }
                regel(produkt, 1.juni) { multiplikasjon(grunntall, faktorB) }
            }

    @Test
    fun `skal si avhengigheter og produserer`() {
        val regelsett =
            Regelsett("regelsett") {
                regel(produkt, 1.januar) { multiplikasjon(grunntall, faktorA) }
                regel(produkt, 1.juni) { multiplikasjon(grunntall, faktorB) }
                regel(faktorB, 1.juni) { multiplikasjon(grunntall, faktorA) }
            }
        regelsett.produserer.shouldContainExactly(produkt, faktorB)
        regelsett.avhengerAv.shouldContainExactly(grunntall, faktorA)

        regelsett.produserer(produkt) shouldBe true
        regelsett.avhengerAv(grunntall) shouldBe true
        regelsett.avhengerAv(faktorA) shouldBe true
        regelsett.avhengerAv(faktorB) shouldBe false

        regelsett.produserer(grunntall) shouldBe false
        regelsett.avhengerAv(produkt) shouldBe false
    }

    @Test
    fun `regelkjøring i januar skal bruke regler for januar`() {
        with(Opplysninger()) {
            val regelkjøring = Regelkjøring(10.januar, this, regelsett)
            assertEquals(2, regelkjøring.trenger(produkt).size)
            leggTil(Faktum(grunntall, 3.0))
            leggTil(Faktum(faktorA, 1.0))
            finnOpplysning(produkt).verdi shouldBe 3.0
        }
    }

    @Test
    fun `regelkjøring i juni skal bruke regler for juni`() {
        with(Opplysninger()) {
            val regelkjøring = Regelkjøring(10.juni, this, regelsett)
            assertEquals(2, regelkjøring.trenger(produkt).size)
            leggTil(Faktum(grunntall, 3.0))
            leggTil(Faktum(faktorB, 2.0))
            finnOpplysning(produkt).verdi shouldBe 6.0
        }
    }

    @Test
    fun `regelkjøring i juni skal gjenbruke verdi fra januar`() {
        with(Opplysninger(listOf(Faktum(produkt, 3.0)))) {
            val regelkjøring = Regelkjøring(10.juni, this, regelsett)
            assertEquals(0, regelkjøring.trenger(produkt).size)
            finnOpplysning(produkt).verdi shouldBe 3.0
        }
    }
}
