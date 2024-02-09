package no.nav.dagpenger.behandling

import io.kotest.matchers.shouldBe
import no.nav.dagpenger.behandling.regel.multiplikasjon
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class RegelsettTest {
    private val grunntall = Opplysningstype<Double>("A")
    private val faktorA = Opplysningstype<Double>("FaktorA")
    private val faktorB = Opplysningstype<Double>("FaktorB")
    private val produserer = Opplysningstype<Double>("Output")
    private val regelsett
        get() =
            Regelsett("regelsett") {
                regel(1.januar) { produserer.multiplikasjon(grunntall, faktorA) }
                regel(1.juni) { produserer.multiplikasjon(grunntall, faktorB) }
            }

    @Test
    fun `regelkjøring i januar skal bruke regler for januar`() {
        with(Opplysninger()) {
            val regelkjøring = Regelkjøring(10.januar, this, regelsett)
            assertEquals(2, regelkjøring.trenger(produserer).size)
            leggTil(Faktum(grunntall, 3.0))
            leggTil(Faktum(faktorA, 1.0))
            finnOpplysning(produserer).verdi shouldBe 3.0
        }
    }

    @Test
    fun `regelkjøring i juni skal bruke regler for juni`() {
        with(Opplysninger()) {
            val regelkjøring = Regelkjøring(10.juni, this, regelsett)
            assertEquals(2, regelkjøring.trenger(produserer).size)
            leggTil(Faktum(grunntall, 3.0))
            leggTil(Faktum(faktorB, 2.0))
            finnOpplysning(produserer).verdi shouldBe 6.0
        }
    }

    @Test
    fun `regelkjøring i juni skal gjenbruke verdi fra januar`() {
        with(Opplysninger(listOf(Faktum(produserer, 3.0)))) {
            val regelkjøring = Regelkjøring(10.juni, this, regelsett)
            assertEquals(0, regelkjøring.trenger(produserer).size)
            finnOpplysning(produserer).verdi shouldBe 3.0
        }
    }
}
