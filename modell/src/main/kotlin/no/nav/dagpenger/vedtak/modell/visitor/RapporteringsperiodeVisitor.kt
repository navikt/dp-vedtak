package no.nav.dagpenger.vedtak.modell.visitor

import no.nav.dagpenger.vedtak.modell.rapportering.Rapporteringsperiode
import java.util.UUID

interface RapporteringsperiodeVisitor : DagVisitor {
    fun preVisitRapporteringsperiode(rapporteringsperiodeId: UUID, periode: Rapporteringsperiode) {}
    fun postVisitRapporteringsperiode(rapporteringsperiodeId: UUID, periode: Rapporteringsperiode) {}
}
