package no.nav.dagpenger.vedtak.modell.visitor

import no.nav.dagpenger.vedtak.modell.rapportering.Dag
import no.nav.dagpenger.vedtak.modell.rapportering.Rapporteringsperiode

interface RapporteringsperiodeVisitor {
    fun preVisitRapporteringsperiode(rapporteringsperiode: Rapporteringsperiode) {}
    fun visitdag(dag: Dag) {}
    fun postVisitRapporteringsperiode(rapporteringsperiode: Rapporteringsperiode) {}
}
