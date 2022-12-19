package no.nav.dagpenger.vedtak.modell.visitor

import no.nav.dagpenger.vedtak.modell.PersonIdentifikator

interface PersonVisitor : VedtakHistorikkVisitor, BeregningHistorikkVisitor {

    fun visitPerson(personIdentifikator: PersonIdentifikator) {}
}
