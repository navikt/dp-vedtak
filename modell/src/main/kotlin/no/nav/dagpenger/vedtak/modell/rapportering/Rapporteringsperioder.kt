package no.nav.dagpenger.vedtak.modell.rapportering

import no.nav.dagpenger.vedtak.modell.hendelser.RapporteringHendelse
import no.nav.dagpenger.vedtak.modell.rapportering.Rapporteringsperiode.Companion.merge
import no.nav.dagpenger.vedtak.modell.visitor.PersonVisitor

class Rapporteringsperioder(perioder: List<Rapporteringsperiode> = mutableListOf()) {
    private var perioder: List<Rapporteringsperiode> = perioder.toMutableList()

    fun håndter(rapporteringHendelse: RapporteringHendelse) {
        val rapporteringsperiode = rapporteringHendelse.populerRapporteringsperiode()
        merge(rapporteringsperiode)
    }

    fun accept(visitor: PersonVisitor) {
        perioder.forEach { it.accept(visitor) }
    }

    private fun merge(rapporteringsperiode: Rapporteringsperiode) {
        perioder = this.perioder.merge(rapporteringsperiode).toMutableList()
    }
}
