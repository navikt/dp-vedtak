package no.nav.dagpenger.opplysning

import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import no.nav.dagpenger.opplysning.TestOpplysningstyper.beløpA
import no.nav.dagpenger.opplysning.TestOpplysningstyper.beløpB
import no.nav.dagpenger.opplysning.TestOpplysningstyper.faktorA
import no.nav.dagpenger.opplysning.TestOpplysningstyper.faktorB
import no.nav.dagpenger.opplysning.TestOpplysningstyper.grunntall
import no.nav.dagpenger.opplysning.regel.multiplikasjon
import no.nav.dagpenger.opplysning.verdier.Beløp
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class RegelsettTest {
    private val regelsett
        get() =
            Regelsett("regelsett") {
                regel(beløpA, 1.januar) { multiplikasjon(grunntall, faktorA) }
                regel(beløpA, 1.juni) { multiplikasjon(grunntall, faktorB) }
            }

    @Test
    fun `skal si avhengigheter og produserer`() {
        val regelsett =
            Regelsett("regelsett") {
                regel(beløpA, 1.januar) { multiplikasjon(grunntall, faktorA) }
                regel(beløpA, 1.juni) { multiplikasjon(grunntall, faktorB) }
                regel(beløpB, 1.juni) { multiplikasjon(grunntall, faktorA) }
            }
        regelsett.produserer.shouldContainExactly(beløpA, beløpB)
        regelsett.avhengerAv.shouldContainExactly(grunntall, faktorA, faktorB)
        // todo: test at avhengener av ikke inneholder opplysninger en produserer selv. Dette er en svakhet i dag.

        regelsett.produserer(beløpA) shouldBe true
        regelsett.avhengerAv(grunntall) shouldBe true
        regelsett.avhengerAv(faktorA) shouldBe true
        regelsett.avhengerAv(faktorB) shouldBe true

        regelsett.produserer(grunntall) shouldBe false
        regelsett.avhengerAv(beløpA) shouldBe false
    }

    @Test
    fun `regelkjøring i januar skal bruke regler for januar`() {
        with(Opplysninger()) {
            val regelkjøring = Regelkjøring(10.januar, this, regelsett)
            assertEquals(2, regelkjøring.trenger(beløpA).size)
            leggTil(Faktum(grunntall, Beløp(3.0)))
            leggTil(Faktum(faktorA, 1.0))
            finnOpplysning(beløpA).verdi shouldBe Beløp(3.0)
        }
    }

    @Test
    fun `regelkjøring i juni skal bruke regler for juni`() {
        with(Opplysninger()) {
            val regelkjøring = Regelkjøring(10.juni, this, regelsett)
            assertEquals(2, regelkjøring.trenger(beløpA).size)
            leggTil(Faktum(grunntall, Beløp(3.0)))
            leggTil(Faktum(faktorB, 2.0))
            finnOpplysning(beløpA).verdi shouldBe Beløp(6.0)
        }
    }

    @Test
    fun `regelkjøring i juni skal gjenbruke verdi fra januar`() {
        with(Opplysninger(listOf(Faktum(beløpA, Beløp(3.0))))) {
            val regelkjøring = Regelkjøring(10.juni, this, regelsett)
            assertEquals(0, regelkjøring.trenger(beløpA).size)
            finnOpplysning(beløpA).verdi shouldBe Beløp(3.0)
        }
    }
}
