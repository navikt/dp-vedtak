package no.nav.dagpenger.vedtak.modell.rapportering

import no.nav.dagpenger.vedtak.modell.hendelser.Rapporteringshendelse
import no.nav.dagpenger.vedtak.modell.visitor.PersonVisitor

class Rapporteringsperioder(val perioder: MutableList<Rapporteringsperiode> = mutableListOf()) {
    fun håndter(rapporteringsHendelse: Rapporteringshendelse): Rapporteringsperiode {
        val rapporteringsperiode = rapporteringsHendelse.populerRapporteringsperiode()
        perioder.add(rapporteringsperiode)
        return rapporteringsperiode
    }

    fun accept(visitor: PersonVisitor) {
        perioder.forEach { it.accept(visitor) }
    }
}
