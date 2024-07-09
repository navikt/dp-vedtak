package no.nav.dagpenger.regel

import no.nav.dagpenger.dag.DAG
import no.nav.dagpenger.dag.printer.MermaidPrinter
import no.nav.dagpenger.opplysning.Regelsett
import no.nav.dagpenger.opplysning.Regelverk
import no.nav.dagpenger.opplysning.dag.RegeltreBygger
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class DiagramTest {
    @Test
    fun `printer hele dagpengeregeltreet`() {
        val bygger =
            RegeltreBygger(
                Alderskrav.regelsett,
                Meldeplikt.regelsett,
                Minsteinntekt.regelsett,
                Opptjeningstid.regelsett,
                ReellArbeidssøker.regelsett,
                KravPåDagpenger.regelsett,
                Rettighetstype.regelsett,
                Søknadstidspunkt.regelsett,
                TapAvArbeidsinntektOgArbeidstid.regelsett,
                Verneplikt.regelsett,
            )

        val regeltre = bygger.dag()
        val mermaidPrinter = MermaidPrinter(regeltre)
        val output = mermaidPrinter.toPrint()
        assertTrue(output.contains("graph RL"))

        println(output)
    }

    @Test
    fun `lager tre av regelsettene`() {
        val regelverk =
            Regelverk(
                Alderskrav.regelsett,
                Meldeplikt.regelsett,
                Minsteinntekt.regelsett,
                Opptjeningstid.regelsett,
                ReellArbeidssøker.regelsett,
                KravPåDagpenger.regelsett,
                Rettighetstype.regelsett,
                Søknadstidspunkt.regelsett,
                TapAvArbeidsinntektOgArbeidstid.regelsett,
                Verneplikt.regelsett,
            )

        regelverk.regeltreFor(KravPåDagpenger.kravPåDagpenger).also {
            val b = MermaidPrinter(it as DAG<Regelsett, Any?>)
            println(b.toPrint())
        }
    }
}
