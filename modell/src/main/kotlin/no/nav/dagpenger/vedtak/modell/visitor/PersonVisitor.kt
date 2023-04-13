package no.nav.dagpenger.vedtak.modell.visitor

import no.nav.dagpenger.vedtak.modell.PersonIdentifikator

interface PersonVisitor : VedtakHistorikkVisitor, RapporteringsperiodeVisitor {

    fun visitPerson(personIdentifikator: PersonIdentifikator) {}
}
