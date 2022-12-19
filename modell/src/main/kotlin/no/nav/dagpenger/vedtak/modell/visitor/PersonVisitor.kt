package no.nav.dagpenger.vedtak.modell.visitor

import no.nav.dagpenger.vedtak.modell.PersonIdentifikator

interface PersonVisitor : VedtakHistorikkVisitor {

    fun visitPerson(personIdentifikator: PersonIdentifikator) {}
}
