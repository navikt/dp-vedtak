package no.nav.dagpenger.opplysning

import io.kotest.matchers.collections.shouldContainExactlyInAnyOrder
import io.kotest.matchers.collections.shouldHaveSize
import no.nav.dagpenger.opplysning.regel.erSann
import no.nav.dagpenger.opplysning.regel.innhentMed
import no.nav.dagpenger.opplysning.regel.innhentes
import org.junit.jupiter.api.Test

class RegelverkTest {
    @Test
    fun `en ding som samler hele regelverk`() {
        val typeA = Opplysningstype.somBoolsk("A")
        val typeB = Opplysningstype.somBoolsk("B")
        val typeC = Opplysningstype.somBoolsk("C")
        val typeE = Opplysningstype.somBoolsk("E")
        val typeF = Opplysningstype.somBoolsk("F")

        val r1 =
            Regelsett("Søknadstidspunkt") {
                regel(typeA) { innhentes }
                regel(typeE) { erSann(typeF) }
            }
        val r2 = Regelsett("Inntekt") { regel(typeB) { innhentMed(typeA) } }
        val r3 = Regelsett("Dagpenger") { regel(typeC) { innhentMed(typeA, typeB) } }

        val regelverk = Regelverk(r1, r2, r3)

        regelverk.regelsettFor(typeC).shouldHaveSize(3)
        regelverk.reglerFor(typeC).shouldHaveSize(4)

        regelverk.regelsettFor(typeC).shouldContainExactlyInAnyOrder(r1, r2, r3)
    }
}
