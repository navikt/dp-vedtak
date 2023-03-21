package no.nav.dagpenger.vedtak.modell.rapportering

import no.nav.dagpenger.vedtak.modell.hendelser.Rapporteringshendelse
import no.nav.dagpenger.vedtak.modell.visitor.PersonVisitor

class Rapporteringsperioder(val perioder: MutableList<Rapporteringsperiode> = mutableListOf()) {
    fun håndter(rapporteringsHendelse: Rapporteringshendelse) {
        val rapporteringsperiode = Rapporteringsperiode(rapporteringsHendelse.rapporteringsId)
        rapporteringsHendelse.populerRapporteringsperiode(rapporteringsperiode)
        perioder.add(rapporteringsperiode)
    }

    fun accept(visitor: PersonVisitor) {
        // visitor.preVisitRapporteringsperioder(this)
        perioder.forEach { it.accept(visitor) }
        // visitor.postVisitRapporteringsperioder(this)
    }
}
