package no.nav.dagpenger.opplysning.dag

import no.nav.dagpenger.opplysning.Regelsett
import no.nav.dagpenger.opplysning.TestOpplysningstyper.faktorA
import no.nav.dagpenger.opplysning.TestOpplysningstyper.faktorB
import no.nav.dagpenger.opplysning.TestOpplysningstyper.produkt
import no.nav.dagpenger.opplysning.regel.multiplikasjon
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import kotlin.test.assertContains

class RegeltreByggerTest {
    @Test
    fun `bygg regeltre`() {
        val regelsett = Regelsett("regelsett") { regel(produkt) { multiplikasjon(faktorA, faktorB) } }

        val regeltre = RegeltreBygger(regelsett.regler()).dag()
        assertEquals(3, regeltre.nodes.size, "Har en node for hver opplysningstype")
        assertEquals(2, regeltre.edges.size, "Har en kobling fra utledet til avhengig opplysningstype")

        val leafNodes = regeltre.findLeafNodes()
        assertEquals(2, leafNodes.size, "Har to løvnoder")

        val opplysningerUtenAvhengigheter = leafNodes.map { it.data }
        assertContains(opplysningerUtenAvhengigheter, faktorA)
        assertContains(opplysningerUtenAvhengigheter, faktorB)
    }
}
