package no.nav.dagpenger.vedtak.modell.visitor

import no.nav.dagpenger.vedtak.modell.Beløp
import java.time.LocalDate

interface BeregnetDagVisitor {
    fun visitDag(dato: LocalDate, beløp: Beløp) {}
}
