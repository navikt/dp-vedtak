package no.nav.dagpenger.vedtak.modell.visitor

import java.time.LocalDate
import java.util.UUID

interface RapporteringsperiodeVisitor : DagVisitor {
    fun preVisitRapporteringsperiode(rapporteringsperiode: UUID, fom: LocalDate, tom: LocalDate) {}
    fun postVisitRapporteringsperiode(rapporteringsperiode: UUID, fom: LocalDate, tom: LocalDate) {}
}
