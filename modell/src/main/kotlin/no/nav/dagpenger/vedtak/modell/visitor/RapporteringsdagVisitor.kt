package no.nav.dagpenger.vedtak.modell.visitor

import no.nav.dagpenger.vedtak.modell.rapportering.Aktivitet
import no.nav.dagpenger.vedtak.modell.rapportering.Rapporteringsdag

interface RapporteringsdagVisitor {
    fun visitRapporteringsdag(
        rapporteringsdag: Rapporteringsdag,
        aktiviteter: List<Aktivitet>,
    ) {}
}
