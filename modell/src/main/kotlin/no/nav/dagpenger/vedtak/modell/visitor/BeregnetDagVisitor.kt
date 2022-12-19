package no.nav.dagpenger.vedtak.modell.visitor

import java.time.LocalDate

interface BeregnetDagVisitor {
    fun visitDag(dato: LocalDate, beløp: Number) {}
}
