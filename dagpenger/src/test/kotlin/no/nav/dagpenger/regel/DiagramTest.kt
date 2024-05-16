package no.nav.dagpenger.regel

import no.nav.dagpenger.opplysning.dag.RegeltreBygger
import no.nav.dagpenger.opplysning.dag.printer.MermaidPrinter
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class DiagramTest {
    @Test
    fun `printer hele dagpengeregeltreet`() {
        val bygger =
            RegeltreBygger(
                Rettighetstype.regelsett,
                Søknadstidspunkt.regelsett,
                Verneplikt.regelsett,
                Alderskrav.regelsett,
                Meldeplikt.regelsett,
                Minsteinntekt.regelsett,
                Opptjeningstid.regelsett,
                ReellArbeidssøker.regelsett,
                RettTilDagpenger.regelsett,
            )

        val regeltre = bygger.dag()
        val mermaidPrinter = MermaidPrinter(regeltre)
        val output = mermaidPrinter.toPrint()
        assertTrue(output.contains("graph RL"))

        println(output)
    }
}
