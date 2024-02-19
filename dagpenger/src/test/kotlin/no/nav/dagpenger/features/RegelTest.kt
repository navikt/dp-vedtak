package no.nav.dagpenger.features

import io.cucumber.java8.No
import no.nav.dagpenger.opplysning.Opplysninger
import no.nav.dagpenger.opplysning.Regelsett
import no.nav.dagpenger.opplysning.dag.RegeltreBygger
import no.nav.dagpenger.opplysning.dag.printer.MermaidPrinter

interface RegelTest : No {
    val opplysninger: Opplysninger
    val regelsett: List<Regelsett>

    fun skrivRegeltre(): String {
        val regeltre = RegeltreBygger(*regelsett.toTypedArray())
        return MermaidPrinter(regeltre.dag()).toPrint()
    }
}
