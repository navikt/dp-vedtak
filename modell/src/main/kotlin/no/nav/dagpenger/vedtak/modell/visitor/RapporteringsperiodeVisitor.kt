package no.nav.dagpenger.vedtak.modell.visitor

import no.nav.dagpenger.vedtak.modell.rapportering.Arbeidsdag
import no.nav.dagpenger.vedtak.modell.rapportering.Fraværsdag
import no.nav.dagpenger.vedtak.modell.rapportering.Helgedag
import no.nav.dagpenger.vedtak.modell.rapportering.Rapporteringsperiode

interface RapporteringsperiodeVisitor {
    fun preVisitRapporteringsperiode(rapporteringsperiode: Rapporteringsperiode) {}
    fun visitArbeidsdag(arbeidsdag: Arbeidsdag) {}
    fun visitHelgedag(helgedag: Helgedag) {}
    fun visitFraværsdag(fraværsdag: Fraværsdag) {}
    fun postVisitRapporteringsperiode(rapporteringsperiode: Rapporteringsperiode) {}
}
