package no.nav.dagpenger.vedtak.modell

import no.nav.dagpenger.vedtak.modell.visitor.BeregnetDagVisitor
import java.time.LocalDate

class BeregnetDag(private val dato: LocalDate, private val beløp: Beløp) {
    fun accept(visitor: BeregnetDagVisitor) {
        visitor.visitDag(dato, beløp)
    }
}
