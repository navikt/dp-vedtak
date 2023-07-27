package no.nav.dagpenger.vedtak.modell.visitor

import no.nav.dagpenger.vedtak.modell.rapportering.Dag
import java.time.LocalDate
import java.util.UUID

interface RapporteringsperiodeVisitor {
    fun preVisitRapporteringsperiode(rapporteringsperiode: UUID, fom: LocalDate, tom: LocalDate) {}
    fun visitdag(dag: Dag) {}
    fun postVisitRapporteringsperiode(rapporteringsperiode: UUID, fom: LocalDate, tom: LocalDate) {}
}
