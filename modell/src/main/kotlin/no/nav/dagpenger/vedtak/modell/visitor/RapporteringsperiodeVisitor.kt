package no.nav.dagpenger.vedtak.modell.visitor

import no.nav.dagpenger.vedtak.modell.rapportering.Fraværsdag
import no.nav.dagpenger.vedtak.modell.rapportering.Helgedag
import no.nav.dagpenger.vedtak.modell.rapportering.MandagTilFredag
import no.nav.dagpenger.vedtak.modell.rapportering.Rapporteringsperiode

interface RapporteringsperiodeVisitor {
    fun preVisitRapporteringsperiode(rapporteringsperiode: Rapporteringsperiode) {}
    fun visitMandagTilFredag(mandagTilFredag: MandagTilFredag) {}
    fun visitHelgedag(helgedag: Helgedag) {}
    fun visitFraværsdag(fraværsdag: Fraværsdag) {}
    fun postVisitRapporteringsperiode(rapporteringsperiode: Rapporteringsperiode) {}
}
