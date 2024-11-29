package no.nav.dagpenger.opplysning.regel

import io.kotest.matchers.shouldBe
import no.nav.dagpenger.opplysning.Faktum
import no.nav.dagpenger.opplysning.Opplysninger
import no.nav.dagpenger.opplysning.Opplysningstype
import org.junit.jupiter.api.Test

class MindreEnnTest {
    private val opplysningstypeA = Opplysningstype.somDesimaltall("a")
    private val opplysningstypeB = Opplysningstype.somDesimaltall("b")
    private val resultat = Opplysningstype.somBoolsk("resultat")

    private val regel = MindreEnn(resultat, opplysningstypeA, opplysningstypeB)

    @Test
    fun `mindre enn`() {
        kjørRegel(mindreEnn(40.0, 18.75)) shouldBe false
        kjørRegel(mindreEnn(18.75, 18.75)) shouldBe false
        kjørRegel(mindreEnn(18.74, 18.75)) shouldBe true
        kjørRegel(mindreEnn(0.0, 18.75)) shouldBe true
        kjørRegel(mindreEnn(0.0, 0.0)) shouldBe false

        kjørRegel(mindreEnn(0.0, -5.0)) shouldBe false
        kjørRegel(mindreEnn(-5.0, 0.0)) shouldBe true
    }

    private fun kjørRegel(opplysninger: Opplysninger) = regel.lagProdukt(opplysninger).verdi

    private fun mindreEnn(
        a: Double,
        b: Double,
    ) = Opplysninger().apply {
        leggTil(Faktum(opplysningstypeA, a))
        leggTil(Faktum(opplysningstypeB, b))
    }
}
